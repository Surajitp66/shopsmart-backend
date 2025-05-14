package com.shopsmart.base.model;

/**
 * Enum for order status
 */
public enum OrderStatus {
    PENDING("Order received, awaiting processing"),
    PROCESSING("Order is being processed"),
    SHIPPED("Order has been shipped"),
    DELIVERED("Order has been delivered"),
    COMPLETED("Order is complete"),
    RETURNED("Order has been returned"),
    CANCELLED("Order has been cancelled");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}