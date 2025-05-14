package com.shopsmart.base.service;

import com.shopsmart.base.dto.InventoryDTO;
import com.shopsmart.base.dto.StockMovementDTO;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.model.Inventory;
import com.shopsmart.base.model.Product;
import com.shopsmart.base.model.StockMovement;
import com.shopsmart.base.model.StockMovementType;
import com.shopsmart.base.repository.InventoryRepository;
import com.shopsmart.base.repository.ProductRepository;
import com.shopsmart.base.repository.StockMovementRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for inventory management operations
 */
@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final StockMovementRepository stockMovementRepository;

    /**
     * Constructor with dependencies
     *
     * @param inventoryRepository     the inventory repository
     * @param productRepository       the product repository
     * @param stockMovementRepository the stock movement repository
     */
    @Autowired
    public InventoryService(InventoryRepository inventoryRepository,
                            ProductRepository productRepository,
                            StockMovementRepository stockMovementRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    /**
     * Get all inventory items
     *
     * @return list of all inventory items
     */
    public List<InventoryDTO> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get inventory by product ID
     *
     * @param productId the product ID
     * @return inventory for the specified product
     * @throws ResourceNotFoundException if inventory not found
     */
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            throw new ResourceNotFoundException("Inventory not found for product id: " + productId);
        }
        return convertToDTO(inventory);
    }

    /**
     * Get low stock items
     *
     * @return list of low stock items
     */
    public List<InventoryDTO> getLowStockItems() {
        return inventoryRepository.findLowStockItems().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get items that need to be reordered
     *
     * @return list of items to reorder
     */
    public List<InventoryDTO> getItemsToReorder() {
        return inventoryRepository.findItemsToReorder().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Adjust inventory stock
     *
     * @param productId    the product ID
     * @param quantity     the quantity to adjust
     * @param movementType the type of stock movement
     * @param reference    the reference for the adjustment
     * @return updated inventory
     * @throws ResourceNotFoundException if product not found
     * @throws IllegalArgumentException  if operation is invalid
     */
    @Transactional
    public InventoryDTO adjustStock(Long productId, int quantity, StockMovementType movementType, String reference) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            // Create new inventory record if not exists
            inventory = new Inventory();
            inventory.setProduct(product);
            inventory.setQuantity(0);
            inventory.setLowStockThreshold(5); // Default values
            inventory.setReorderPoint(10);
            inventory = inventoryRepository.save(inventory);
        }

        // Apply stock adjustment
        int oldQuantity = inventory.getQuantity();
        int newQuantity;
        LocalDateTime now = LocalDateTime.now();

        switch (movementType) {
            case PURCHASE:
            case RETURN:
            case ADJUSTMENT_ADD:
                newQuantity = oldQuantity + quantity;
                inventory.setLastRestockDate(now);
                break;
            case SALE:
            case DAMAGE:
            case ADJUSTMENT_SUBTRACT:
                if (oldQuantity < quantity) {
                    throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
                }
                newQuantity = oldQuantity - quantity;
                break;
            default:
                throw new IllegalArgumentException("Invalid stock movement type");
        }

        inventory.setQuantity(newQuantity);
        Inventory updatedInventory = inventoryRepository.save(inventory);

        // Record stock movement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setQuantity(quantity);
        movement.setMovementType(movementType);
        movement.setOldQuantity(oldQuantity);
        movement.setNewQuantity(newQuantity);
        movement.setReference(reference);
        movement.setMovementDate(now);
        stockMovementRepository.save(movement);

        return convertToDTO(updatedInventory);
    }

    /**
     * Update inventory settings
     *
     * @param productId    the product ID
     * @param inventoryDTO the inventory settings to update
     * @return updated inventory
     * @throws ResourceNotFoundException if inventory not found
     */
    @Transactional
    public InventoryDTO updateInventorySettings(Long productId, InventoryDTO inventoryDTO) {
        Inventory inventory = inventoryRepository.findByProductId(productId);
        if (inventory == null) {
            throw new ResourceNotFoundException("Inventory not found for product id: " + productId);
        }

        if (inventoryDTO.getLowStockThreshold() != null) {
            inventory.setLowStockThreshold(inventoryDTO.getLowStockThreshold());
        }

        if (inventoryDTO.getReorderPoint() != null) {
            inventory.setReorderPoint(inventoryDTO.getReorderPoint());
        }

        if (inventoryDTO.getReorderQuantity() != null) {
            // Assuming reorderQuantity is added to Inventory entity
            // If not in the original entity, you would need to add it
        }

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return convertToDTO(updatedInventory);
    }

    /**
     * Get stock movement history for a product
     *
     * @param productId the product ID
     * @return list of stock movements for the product
     */
    public List<StockMovementDTO> getStockMovementHistory(Long productId) {
        return stockMovementRepository.findByProductIdOrderByMovementDateDesc(productId).stream()
                .map(this::convertMovementToDTO)
                .collect(Collectors.toList());
    }
    /**
     * Convert Inventory entity to InventoryDTO
     *
     * @param inventory the inventory entity
     * @return the inventory DTO
     */
    private InventoryDTO convertToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProduct().getId());
        dto.setProductName(inventory.getProduct().getName());
        dto.setProductSku(inventory.getProduct().getSku());
        dto.setQuantity(inventory.getQuantity());
        dto.setLowStockThreshold(inventory.getLowStockThreshold());
        dto.setReorderPoint(inventory.getReorderPoint());
        dto.setLastRestockDate(inventory.getLastRestockDate());
        // Assuming these fields exist in the Inventory entity
        // If not available in entity, they may need to be calculated or left null
        dto.setReorderQuantity(inventory.getReorderQuantity());
        dto.setLastUpdated(inventory.getLastUpdated());

        // Set status flags
        int quantity = inventory.getQuantity();

        // inStock flag - true if quantity > 0
        dto.setInStock(quantity > 0);

        // lowStock flag - true if quantity <= lowStockThreshold but > 0
        dto.setLowStock(quantity > 0 && quantity <= inventory.getLowStockThreshold());

        // needsReorder flag - true if quantity <= reorderPoint
        dto.setNeedsReorder(quantity <= inventory.getReorderPoint());

        return dto;
    }

    /**
     * Convert StockMovement entity to StockMovementDTO
     *
     * @param stockMovement the stock movement entity
     * @return the stock movement DTO
     */
    private StockMovementDTO convertMovementToDTO(StockMovement stockMovement) {
        StockMovementDTO dto = new StockMovementDTO();
        dto.setId(stockMovement.getId());
        dto.setProductId(stockMovement.getProduct().getId());
        dto.setProductName(stockMovement.getProduct().getName());
        dto.setQuantity(stockMovement.getQuantity());
        dto.setMovementType(stockMovement.getMovementType());
        dto.setOldQuantity(stockMovement.getOldQuantity());
        dto.setNewQuantity(stockMovement.getNewQuantity());
        dto.setReference(stockMovement.getReference());
        dto.setMovementDate(stockMovement.getMovementDate());
        return dto;
    }
}