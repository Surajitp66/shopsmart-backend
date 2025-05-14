package com.shopsmart.base.dto;

import com.shopsmart.base.model.StockMovementType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for stock adjustments
 */
public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "Movement type is required")
    private StockMovementType movementType;

    private String reference;

    // Default constructor
    public StockAdjustmentRequest() {
    }

    // Constructor with fields
    public StockAdjustmentRequest(Long productId, Integer quantity, StockMovementType movementType, String reference) {
        this.productId = productId;
        this.quantity = quantity;
        this.movementType = movementType;
        this.reference = reference;
    }

    // Getters and Setters
    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public StockMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(StockMovementType movementType) {
        this.movementType = movementType;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}