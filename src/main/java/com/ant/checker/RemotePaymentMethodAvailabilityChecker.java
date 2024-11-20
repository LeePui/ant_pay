package com.ant.checker;

import com.ant.pay.PaymentType;

import static com.ant.Constant.REMOTE_REQUEST_AVG_TIME_MILLIS;

/**
 * @description: 远程检查支付方式是否可用实现
 * @className: RemotePaymentMethodAvailabilityChecker
 * @author: lee
 **/
public class RemotePaymentMethodAvailabilityChecker implements PaymentMethodAvailabilityChecker {

    /**
     * 这里通过模拟远程调用来判断支付方式是否可用：
     * 1. 休眠模拟网络延迟,服务超时
     * 2. 随机数模拟返回是否可用
     *
     * @param paymentType 支付方式类型
     * @return true 可用，false 不可用
     */
    @Override
    public boolean isAvailable(PaymentType paymentType) throws InterruptedException {
        Thread.sleep((long) (Math.random() * REMOTE_REQUEST_AVG_TIME_MILLIS));
        // 90% 概率可用
        return Math.random() > 0.1;
    }
}
