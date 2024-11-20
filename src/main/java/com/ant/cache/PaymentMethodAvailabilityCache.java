package com.ant.cache;

import com.ant.pay.PaymentMethod;
import com.ant.pay.PaymentType;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description: 支付方式可用性缓存
 * @className: PaymentMethodAvailabilityCache
 * @author: lee
 **/
public class PaymentMethodAvailabilityCache {

    // key: 支付方式类型，value: 支付方式相关信息（可用性等）
    private final Map<PaymentType, PaymentMethod> cache = new ConcurrentHashMap<>();

    public void updatePaymentMethod(PaymentMethod method) {
        cache.put(method.getType(), method);
    }

    public List<PaymentMethod> getAllPaymentMethods() {
        return cache.values().stream().toList();
    }

    public PaymentMethod getPaymentMethod(PaymentType paymentType) {
        return cache.get(paymentType);
    }

}
