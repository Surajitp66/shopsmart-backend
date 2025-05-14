package com.shopsmart.base.repository;

import com.shopsmart.base.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * Repository interface for Inventory entity
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product ID
     *
     * @param productId the product ID
     * @return the inventory record for the product
     */
    Inventory findByProductId(Long productId);

    /**
     * Find items with stock level at or below low stock threshold
     *
     * @return list of low stock inventory items
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.lowStockThreshold")
    List<Inventory> findLowStockItems();

    /**
     * Find items with stock level at or below reorder point
     *
     * @return list of inventory items that need to be reordered
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderPoint")
    List<Inventory> findItemsToReorder();

    /**
     * Find items that are out of stock (quantity = 0)
     *
     * @return list of out of stock inventory items
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantity = 0")
    List<Inventory> findOutOfStockItems();

    /**
     * Find items based on product category
     *
     * @param categoryId the category ID
     * @return list of inventory items in the specified category
     */
    @Query("SELECT i FROM Inventory i JOIN i.product p WHERE p.category.id = :categoryId")
    List<Inventory> findByProductCategoryId(Long categoryId);
}
