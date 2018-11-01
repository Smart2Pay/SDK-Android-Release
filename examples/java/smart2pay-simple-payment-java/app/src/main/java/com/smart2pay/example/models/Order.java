package com.smart2pay.example.models;

import com.smart2pay.sdk.models.Payment;

public final class Order {
    private Payment.PaymentProvider type = Payment.PaymentProvider.NONE;
    private int amount = 0;
    private String currency = "";

    public final Payment.PaymentProvider getType() {
        return this.type;
    }

    public final void setType(Payment.PaymentProvider value) {
        this.type = value;
    }

    public final int getAmount() {
        return this.amount;
    }

    public final void setAmount(int value) {
        this.amount = value;
    }

    public final String getCurrency() {
        return this.currency;
    }

    public final void setCurrency(String value) {
        this.currency = value;
    }

    public Order() {
        this.type = Payment.PaymentProvider.NONE;
        this.currency = "";
    }
}
