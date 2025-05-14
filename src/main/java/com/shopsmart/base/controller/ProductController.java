package com.shopsmart.base.controller;

import com.shopsmart.base.dto.ApiResponse;
import com.shopsmart.base.dto.ProductDTO;
import com.shopsmart.base.dto.ProductSearchCriteria;
import com.shopsmart.base.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RESTful controller for product operations
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Get all products with pagination and sorting
     *
     * @param page page number (zero-based)
     * @param size items per page
     * @param sort sort field
     * @param direction sort direction
     * @return paginated list of products
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {

        Pageable pageable = PageRequest.of(page, size,
                Sort.Direction.fromString(direction), sort);

        Page<ProductDTO> productPage = productService.getAllProducts(pageable);

        Map<String, Object> response = createPaginatedResponse(productPage);

        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", response));
    }

    /**
     * Get products by category with pagination
     *
     * @param categoryId category identifier
     * @param page page number (zero-based)
     * @param size items per page
     * @return paginated list of products in the category
     */
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productPage = productService.getProductsByCategory(categoryId, pageable);

        Map<String, Object> response = createPaginatedResponse(productPage);

        return ResponseEntity.ok(new ApiResponse(true, "Products retrieved successfully", response));
    }

    /**
     * Get featured products
     *
     * @param limit maximum number of products to return
     * @return list of featured products
     */
    @GetMapping("/featured")
    public ResponseEntity<ApiResponse> getFeaturedProducts(
            @RequestParam(defaultValue = "8") int limit) {

        List<ProductDTO> featuredProducts = productService.getFeaturedProducts(limit);

        return ResponseEntity.ok(new ApiResponse(true, "Featured products retrieved successfully", featuredProducts));
    }

    /**
     * Search products with various criteria
     *
     * @param criteria search criteria
     * @param page page number (zero-based)
     * @param size items per page
     * @return paginated search results
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchProducts(
            @ModelAttribute ProductSearchCriteria criteria,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> productPage = productService.searchProducts(criteria, pageable);

        Map<String, Object> response = createPaginatedResponse(productPage);

        return ResponseEntity.ok(new ApiResponse(true, "Search results retrieved", response));
    }

    /**
     * Get product by ID
     *
     * @param id product identifier
     * @return product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getProductById(@PathVariable Long id) {
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
    }

    /**
     * Get product by SKU
     *
     * @param sku product SKU
     * @return product details
     */
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ApiResponse> getProductBySku(@PathVariable String sku) {
        ProductDTO product = productService.getProductBySku(sku);
        return ResponseEntity.ok(new ApiResponse(true, "Product retrieved successfully", product));
    }

    /**
     * Create a new product
     *
     * @param productDTO product data
     * @return created product
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(
                new ApiResponse(true, "Product created successfully", createdProduct),
                HttpStatus.CREATED);
    }

    /**
     * Update an existing product
     *
     * @param id product identifier
     * @param productDTO updated product data
     * @return updated product
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {

        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(new ApiResponse(true, "Product updated successfully", updatedProduct));
    }

    /**
     * Delete a product
     *
     * @param id product identifier
     * @return success message
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ApiResponse(true, "Product deleted successfully"));
    }

    /**
     * Add image to a product
     *
     * @param productId product identifier
     * @param imageUrl image URL
     * @param altText alternative text for the image
     * @return success message
     */
    @PostMapping("/{productId}/images")
    public ResponseEntity<ApiResponse> addProductImage(
            @PathVariable Long productId,
            @RequestParam String imageUrl,
            @RequestParam(required = false) String altText) {

        productService.addProductImage(productId, imageUrl, altText);
        return ResponseEntity.ok(new ApiResponse(true, "Product image added successfully"));
    }

    /**
     * Helper method to create a standardized paginated response
     *
     * @param page page object from Spring Data
     * @return map with pagination details and content
     */
    private Map<String, Object> createPaginatedResponse(Page<ProductDTO> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        response.put("currentPage", page.getNumber());
        response.put("totalItems", page.getTotalElements());
        response.put("totalPages", page.getTotalPages());
        return response;
    }
}
