package com.ant.pay;

/**
 * @description: 支付方式实体类
 * @className: PaymentMethod
 * @author: lee
 **/
public class PaymentMethod {

    private String id;
    private String name;
    private PaymentType type;
    private boolean available;

    private PaymentMethod(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.type = builder.type;
        this.available = builder.available;
    }

    public static class Builder {
        private String id;
        private String name;
        private PaymentType type;
        private boolean available;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(PaymentType type) {
            this.type = type;
            return this;
        }

        public Builder available(boolean available) {
            this.available = available;
            return this;
        }

        public PaymentMethod build() {
            // 省略一些参数校验,checkNullEmptyString
            return new PaymentMethod(this);
        }
    }

    @Override
    public String toString() {
        return "PaymentMethod{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type=" + type +
                ", available=" + available +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PaymentType getType() {
        return type;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
