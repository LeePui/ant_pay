package com.ant.pay;

/**
 * @description: 支付方式枚举类
 * @className: PaymentType
 * @author: lee
 **/
public enum PaymentType {

    BALANCE("1", "余额支付"),
    RED_PACKET("2", "红包支付"),
    COUPON("3", "优惠券支付"),
    VOUCHER("4", "代金券支付");

    private final String id;
    private final String desc;

    PaymentType(String id, String desc) {
        this.id = id;
        this.desc = desc;
    }

    public String getId() {
        return id;
    }

    public String getDesc() {
        return desc;
    }
}
