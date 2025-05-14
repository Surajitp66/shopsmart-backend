package com.shopsmart.base.dto;


import jakarta.validation.constraints.Min;

/**
 * Request DTO for updating inventory settings
 */
public class InventoryUpdateRequest {

    @Min(value = 0, message = "Low stock threshold must be at least 0")
    private Integer lowStockThreshold;

    @Min(value = 0, message = "Reorder point must be at least 0")
    private Integer reorderPoint;

    @Min(value = 1, message = "Reorder quantity must be at least 1")
    private Integer reorderQuantity;

    // Default constructor
    public InventoryUpdateRequest() {
    }

    // Constructor with fields
    public InventoryUpdateRequest(Integer lowStockThreshold, Integer reorderPoint, Integer reorderQuantity) {
        this.lowStockThreshold = lowStockThreshold;
        this.reorderPoint = reorderPoint;
        this.reorderQuantity = reorderQuantity;
    }

    // Getters and Setters
    public Integer getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(Integer lowStockThreshold) {
        this.lowStockThreshold = lowStockThreshold;
    }

    public Integer getReorderPoint() {
        return reorderPoint;
    }

    public void setReorderPoint(Integer reorderPoint) {
        this.reorderPoint = reorderPoint;
    }

    public Integer getReorderQuantity() {
        return reorderQuantity;
    }

    public void setReorderQuantity(Integer reorderQuantity) {
        this.reorderQuantity = reorderQuantity;
    }
}
