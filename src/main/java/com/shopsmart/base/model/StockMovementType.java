package com.shopsmart.base.model;

/**
 * Enum representing various types of stock movements
 */
public enum StockMovementType {
    PURCHASE,       // Stock added due to purchase from supplier
    SALE,           // Stock reduced due to sale
    RETURN,         // Stock added due to customer return
    DAMAGE,         // Stock reduced due to damage or expiry
    ADJUSTMENT_ADD, // Stock added due to manual adjustment (e.g., inventory count correction)
    ADJUSTMENT_SUBTRACT // Stock reduced due to manual adjustment
}