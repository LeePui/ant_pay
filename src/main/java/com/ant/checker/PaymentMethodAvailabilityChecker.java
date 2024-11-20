package com.ant.checker;

import com.ant.pay.PaymentType;

/**
 * @description: 检查支付方式是否可用的接口
 * @className: PaymentMethodAvailabilityChecker
 * @author: lee
 **/
public interface PaymentMethodAvailabilityChecker {

    /**
     * 检查支付方式是否可用
     *
     * @param paymentType 支付方式
     * @return true 可用，false 不可用
     */
    boolean isAvailable(PaymentType paymentType) throws InterruptedException;
}
