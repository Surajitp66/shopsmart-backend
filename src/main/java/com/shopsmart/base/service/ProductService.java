package com.shopsmart.base.service;

import com.shopsmart.base.dto.ProductDTO;
import com.shopsmart.base.dto.ProductSearchCriteria;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.model.Category;
import com.shopsmart.base.model.Inventory;
import com.shopsmart.base.model.Product;
import com.shopsmart.base.model.ProductImage;
import com.shopsmart.base.repository.CategoryRepository;
import com.shopsmart.base.repository.InventoryRepository;
import com.shopsmart.base.repository.ProductRepository;
import com.shopsmart.base.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final ReviewRepository reviewRepository;

    @Autowired
    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository,
                          InventoryRepository inventoryRepository,
                          ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.inventoryRepository = inventoryRepository;
        this.reviewRepository = reviewRepository;
    }

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::convertToDTO);
    }

    public List<ProductDTO> getFeaturedProducts(int limit) {
        return productRepository.findFeaturedProducts(Pageable.ofSize(limit)).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Page<ProductDTO> searchProducts(ProductSearchCriteria criteria, Pageable pageable) {
        if (criteria.getKeyword() != null && !criteria.getKeyword().isEmpty()) {
            return productRepository.searchProducts(criteria.getKeyword(), pageable)
                    .map(this::convertToDTO);
        } else if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null) {
            return productRepository.findByPriceRange(criteria.getMinPrice(), criteria.getMaxPrice(), pageable)
                    .map(this::convertToDTO);
        } else if (criteria.getTagName() != null && !criteria.getTagName().isEmpty()) {
            return productRepository.findByTagName(criteria.getTagName(), pageable)
                    .map(this::convertToDTO);
        } else {
            return productRepository.findAll(pageable)
                    .map(this::convertToDTO);
        }
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return convertToDTO(product);
    }

    public ProductDTO getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with SKU: " + sku));
        return convertToDTO(product);
    }

    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = convertToEntity(productDTO);
        Product savedProduct = productRepository.save(product);

        // Create inventory record for the product
        Inventory inventory = new Inventory();
        inventory.setProduct(savedProduct);
        inventory.setQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
        inventoryRepository.save(inventory);

        return convertToDTO(savedProduct);
    }

    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setSku(productDTO.getSku());
        product.setPrice(productDTO.getPrice());
        product.setSalePrice(productDTO.getSalePrice());
        product.setActive(productDTO.isActive());
        product.setFeatured(productDTO.isFeatured());

        if (productDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " +
                            productDTO.getCategoryId()));
            product.setCategory(category);
        }

        // Update inventory
        Inventory inventory = inventoryRepository.findByProductId(id);
        if (inventory != null && productDTO.getStockQuantity() != null) {
            inventory.setQuantity(productDTO.getStockQuantity());
            inventoryRepository.save(inventory);
        }

        Product updatedProduct = productRepository.save(product);
        return convertToDTO(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(product);
    }

    @Transactional
    public void addProductImage(Long productId, String imageUrl, String altText) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(imageUrl);
        image.setAltText(altText);

        product.addImage(image);
        productRepository.save(product);
    }

    private ProductDTO convertToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setSku(product.getSku());
        dto.setPrice(product.getPrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setActive(product.isActive());
        dto.setFeatured(product.isFeatured());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        // Add images
        dto.setImages(product.getImages().stream()
                .map(img -> new ProductDTO.ImageDTO(img.getId(), img.getImageUrl(), img.getAltText(), img.isPrimary()))
                .collect(Collectors.toList()));

        // Add inventory information if available
        if (product.getInventory() != null) {
            dto.setStockQuantity(product.getInventory().getQuantity());
            dto.setInStock(product.getInventory().getQuantity() > 0);
        }

        // Add rating information
        Double avgRating = reviewRepository.findAverageRatingByProductId(product.getId());
        if (avgRating != null) {
            dto.setAverageRating(avgRating);
        }
        dto.setReviewCount(reviewRepository.countByProductIdAndIsApproved(product.getId(), true));

        return dto;
    }

    private Product convertToEntity(ProductDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setSku(dto.getSku());
        product.setPrice(dto.getPrice());
        product.setSalePrice(dto.getSalePrice());
        product.setActive(dto.isActive());
        product.setFeatured(dto.isFeatured());

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " +
                            dto.getCategoryId()));
            product.setCategory(category);
        }

        return product;
    }
}
