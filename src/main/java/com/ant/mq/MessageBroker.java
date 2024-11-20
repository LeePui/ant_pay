package com.ant.mq;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * @description: 模拟消息订阅服务
 * @className: MessageBroker
 * @author: lee
 **/
public class MessageBroker {

    // key: topic, value: 消费者列表
    private final Map<String, List<Consumer<PaymentStatusChangeEvent>>> subscribers = new ConcurrentHashMap<>();

    /**
     * 模拟订阅
     */
    public void subscribe(String topic, Consumer<PaymentStatusChangeEvent> subscriber) {
        subscribers.computeIfAbsent(topic, k -> new CopyOnWriteArrayList<>()).add(subscriber);
    }

    /**
     * 模拟发布
     */
    public void publish(String topic, PaymentStatusChangeEvent event) {
        final List<Consumer<PaymentStatusChangeEvent>> consumers = subscribers.get(topic);
        if (consumers != null) {
            consumers.forEach(consumer -> {
                try {
                    consumer.accept(event);
                } catch (Exception e) {
                    System.err.printf("消息订阅者处理异常, topic: %s, event: %s \n", topic, event);
                }
            });
        }
    }
}
