package com.ant.mq;

import com.ant.pay.PaymentType;

/**
 * @description: 支付方式服务状态变更事件
 * @className: PaymentStatusChangeEvent
 * @author: lee
 **/
public class PaymentStatusChangeEvent {

    private PaymentType paymentType;
    private boolean available;

    public PaymentStatusChangeEvent(PaymentType paymentType, boolean available) {
        this.paymentType = paymentType;
        this.available = available;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
