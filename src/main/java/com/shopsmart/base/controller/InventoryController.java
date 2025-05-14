package com.shopsmart.base.controller;

import com.shopsmart.base.dto.*;
import com.shopsmart.base.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for inventory management operations
 */
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    /**
     * Get all inventory items
     *
     * @return response entity with list of all inventory items
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllInventory() {
        List<InventoryDTO> inventoryList = inventoryService.getAllInventory();
        return ResponseEntity.ok(new ApiResponse(true, "Inventory retrieved successfully", inventoryList));
    }

    /**
     * Get inventory by product ID
     *
     * @param productId the product ID
     * @return response entity with inventory for the specified product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse> getInventoryByProductId(@PathVariable Long productId) {
        InventoryDTO inventory = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(new ApiResponse(true, "Inventory retrieved successfully", inventory));
    }

    /**
     * Get low stock items
     *
     * @return response entity with list of low stock items
     */
    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse> getLowStockItems() {
        List<InventoryDTO> lowStockItems = inventoryService.getLowStockItems();
        return ResponseEntity.ok(new ApiResponse(true, "Low stock items retrieved successfully", lowStockItems));
    }

    /**
     * Get items that need to be reordered
     *
     * @return response entity with list of items to reorder
     */
    @GetMapping("/reorder")
    public ResponseEntity<ApiResponse> getItemsToReorder() {
        List<InventoryDTO> itemsToReorder = inventoryService.getItemsToReorder();
        return ResponseEntity.ok(new ApiResponse(true, "Items to reorder retrieved successfully", itemsToReorder));
    }

    /**
     * Adjust inventory stock
     *
     * @param request the stock adjustment request
     * @return response entity with updated inventory
     */
    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse> adjustStock(@Valid @RequestBody StockAdjustmentRequest request) {
        InventoryDTO updatedInventory = inventoryService.adjustStock(
                request.getProductId(),
                request.getQuantity(),
                request.getMovementType(),
                request.getReference()
        );
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse(true, "Stock adjusted successfully", updatedInventory));
    }

    /**
     * Update inventory settings
     *
     * @param productId the product ID
     * @param request the inventory update request
     * @return response entity with updated inventory
     */
    @PutMapping("/product/{productId}/settings")
    public ResponseEntity<ApiResponse> updateInventorySettings(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request) {

        InventoryDTO dto = new InventoryDTO();
        dto.setLowStockThreshold(request.getLowStockThreshold());
        dto.setReorderPoint(request.getReorderPoint());
        dto.setReorderQuantity(request.getReorderQuantity());

        InventoryDTO updatedInventory = inventoryService.updateInventorySettings(productId, dto);
        return ResponseEntity.ok(new ApiResponse(true, "Inventory settings updated successfully", updatedInventory));
    }

    /**
     * Get stock movement history for a product
     *
     * @param productId the product ID
     * @return response entity with stock movement history
     */
    @GetMapping("/product/{productId}/movements")
    public ResponseEntity<ApiResponse> getStockMovementHistory(@PathVariable Long productId) {
        List<StockMovementDTO> movements = inventoryService.getStockMovementHistory(productId);
        return ResponseEntity.ok(new ApiResponse(true, "Stock movement history retrieved successfully", movements));
    }
}