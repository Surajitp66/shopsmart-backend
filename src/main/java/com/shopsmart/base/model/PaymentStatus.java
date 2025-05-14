package com.shopsmart.base.model;

/**
 * Enum for payment status
 */
public enum PaymentStatus {
    PENDING("Payment not yet received"),
    AUTHORIZED("Payment authorized but not captured"),
    PAID("Payment received"),
    PARTIALLY_REFUNDED("Payment partially refunded"),
    REFUNDED("Payment fully refunded"),
    FAILED("Payment failed");

    private final String description;

    PaymentStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}