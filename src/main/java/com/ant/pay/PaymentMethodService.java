package com.ant.pay;

import com.ant.Constant;
import com.ant.cache.PaymentMethodAvailabilityCache;
import com.ant.checker.PaymentMethodAvailabilityChecker;
import com.ant.mq.MessageBroker;
import com.ant.mq.PaymentStatusChangeEvent;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

/**
 * @description: 支付方式查询服务
 * @className: PaymentMethodService
 * @author: lee
 **/
public class PaymentMethodService {

    private final PaymentMethodAvailabilityCache cache;
    private final PaymentMethodAvailabilityChecker checker;
    private final MessageBroker messageBroker;
    private final ScheduledExecutorService scheduler;
    private final ExecutorService executorService;
    private final CountDownLatch initLatch;
    private volatile boolean initialized = false;

    public PaymentMethodService(PaymentMethodAvailabilityCache cache, PaymentMethodAvailabilityChecker checker, MessageBroker messageBroker) {
        this.cache = cache;
        this.checker = checker;
        this.messageBroker = messageBroker;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        executorService = Executors.newFixedThreadPool(PaymentType.values().length);
        this.initLatch = new CountDownLatch(1);

        init();
    }

    private void init() {
        // 初始化缓存
        refreshCache();

        // 定时刷新
        scheduler.scheduleWithFixedDelay(this::refreshCache, Constant.CACHE_REFRESH_INTERVAL_SECONDS, Constant.CACHE_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);

        // 模拟启动消息监听
        subscribeToAvailabilityChanges();
    }

    /**
     * 获取所有可用的支付方式
     */
    public List<PaymentMethod> getAvailablePaymentMethods() {
        try {
            if (!initialized && !initLatch.await(Constant.INIT_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                System.err.println("初始化缓存超时");
                return Collections.emptyList();
            }
            return cache.getAllPaymentMethods()
                    .stream()
                    .filter(PaymentMethod::isAvailable)
                    .toList();
        } catch (InterruptedException e) {
            System.err.println("获取所有可用支付方式线程中断");
            return Collections.emptyList();
        }
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        scheduler.shutdown();
        executorService.shutdown();
    }

    private void subscribeToAvailabilityChanges() {
        messageBroker.subscribe(Constant.TOPIC, this::handlerStatusChangeEvent);
    }

    private void handlerStatusChangeEvent(PaymentStatusChangeEvent paymentStatusChangeEvent) {
        final PaymentMethod paymentMethod = cache.getPaymentMethod(paymentStatusChangeEvent.getPaymentType());
        if (paymentMethod != null) {
            paymentMethod.setAvailable(paymentStatusChangeEvent.isAvailable());
            cache.updatePaymentMethod(paymentMethod);
        }
    }

    /**
     * 刷新缓存
     */
    private void refreshCache() {
        System.out.println("刷新缓存...");
        final List<CompletableFuture<PaymentMethod>> completableFutures = Arrays.stream(PaymentType.values()).map(this::createPaymentMethodFuture).toList();
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]))
                .thenAccept(v -> {
                    completableFutures.stream()
                            .map(CompletableFuture::join)
                            .forEach(cache::updatePaymentMethod);

                    // 所有支付方式更新完成后, 设置初始化完成
                    if (!initialized) {
                        initialized = true;
                        initLatch.countDown();
                        System.out.println("支付方式可用性缓存初始化完成");
                    }
                }).exceptionally(throwable -> {
                    System.err.printf("刷新缓存发生异常, %s \n", throwable);
                    if (!initialized) {
                        initialized = true;
                        initLatch.countDown();
                    }
                    return null;
                });

    }


    private CompletableFuture<PaymentMethod> createPaymentMethodFuture(PaymentType paymentType) {
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        boolean available = checker.isAvailable(paymentType);
                        System.out.printf("支付方式可用性检查, 支付方式：%s, 是否可用：%s \n", paymentType.getDesc(), available);
                        return PaymentMethod.builder()
                                .id(paymentType.getId())
                                .type(paymentType)
                                .name(paymentType.getDesc())
                                .available(available)
                                .build();
                    } catch (Exception e) {
                        System.out.printf("检查支付方式可用性调用发生异常, paymentType: %s \n", paymentType);
                        return createUnavailablePaymentMethod(paymentType);
                    }
                }, executorService)
                // 超时设置
                .orTimeout(Constant.REMOTE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                // 超时处理
                .handleAsync((result, throwable) -> {
                    if (throwable != null) {
                        System.out.printf("检查支付方式可用性调用超时, paymentType: %s \n", paymentType.getDesc());
                        return createUnavailablePaymentMethod(paymentType);
                    }
                    return result;
                }, executorService);
    }

    private PaymentMethod createUnavailablePaymentMethod(PaymentType paymentType) {
        return PaymentMethod.builder()
                .id(paymentType.getId())
                .type(paymentType)
                .name(paymentType.getDesc())
                .available(false)
                .build();
    }
}
