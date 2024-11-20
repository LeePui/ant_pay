package com.ant;

import com.ant.cache.PaymentMethodAvailabilityCache;
import com.ant.checker.PaymentMethodAvailabilityChecker;
import com.ant.checker.RemotePaymentMethodAvailabilityChecker;
import com.ant.mq.MessageBroker;
import com.ant.mq.PaymentStatusChangeEvent;
import com.ant.pay.PaymentMethod;
import com.ant.pay.PaymentMethodService;
import com.ant.pay.PaymentType;

import java.util.List;

public class Main {
    public static void main(String[] args) throws InterruptedException {

        // 初始化
        PaymentMethodAvailabilityCache cache = new PaymentMethodAvailabilityCache();
        PaymentMethodAvailabilityChecker checker = new RemotePaymentMethodAvailabilityChecker();
        MessageBroker messageBroker = new MessageBroker();
        PaymentMethodService paymentMethodService = new PaymentMethodService(cache, checker, messageBroker);

        // 第一次获取
        List<PaymentMethod> availablePaymentMethods = paymentMethodService.getAvailablePaymentMethods();
        System.out.println("【第一次获取所有支付方式可用性】：" + availablePaymentMethods);

        // 模拟发布消息: 余额支付方式不可用
        messageBroker.publish(Constant.TOPIC, new PaymentStatusChangeEvent(PaymentType.BALANCE, false));

        availablePaymentMethods = paymentMethodService.getAvailablePaymentMethods();
        System.out.println("【发布余额支付不可用消息后获取所有支付方式可用性】：" + availablePaymentMethods);

        // 休眠等到缓存自动刷新
        System.out.println("【休眠模拟等待缓存自动刷新...】");
        Thread.sleep(Constant.CACHE_REFRESH_INTERVAL_SECONDS * 1000 + 1000);

        availablePaymentMethods = paymentMethodService.getAvailablePaymentMethods();
        System.out.println("【缓存自动刷新后获取所有支付方式可用性】：" + availablePaymentMethods);

        paymentMethodService.shutdown();

    }
}