package com.shopsmart.base.controller;

import com.shopsmart.base.dto.ApiResponse;
import com.shopsmart.base.dto.CategoryDTO;
import com.shopsmart.base.dto.CategoryRequest;
import com.shopsmart.base.dto.CategoryResponse;
import com.shopsmart.base.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Category operations
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @Autowired
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    /**
     * Get all categories
     *
     * @return list of all categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getAllCategories() {
        List<CategoryDTO> categories = categoryService.getAllCategories();
        List<CategoryResponse> responses = categories.stream()
                .map(CategoryResponse::fromDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, "Categories retrieved successfully", responses));
    }

    /**
     * Get all parent categories
     *
     * @return list of parent categories
     */
    @GetMapping("/parents")
    public ResponseEntity<ApiResponse> getAllParentCategories() {
        List<CategoryDTO> categories = categoryService.getAllParentCategories();
        List<CategoryResponse> responses = categories.stream()
                .map(CategoryResponse::fromDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, "Parent categories retrieved successfully", responses));
    }

    /**
     * Get all subcategories for a given parent category ID
     *
     * @param parentId the ID of the parent category
     * @return list of subcategories
     */
    @GetMapping("/parent/{parentId}/subcategories")
    public ResponseEntity<ApiResponse> getSubcategoriesByParentId(@PathVariable Long parentId) {
        List<CategoryDTO> categories = categoryService.getSubcategoriesByParentId(parentId);
        List<CategoryResponse> responses = categories.stream()
                .map(CategoryResponse::fromDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ApiResponse(true, "Subcategories retrieved successfully", responses));
    }

    /**
     * Get a category by ID
     *
     * @param id the category ID
     * @return the category data
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id) {
        CategoryDTO category = categoryService.getCategoryById(id);
        CategoryResponse response = CategoryResponse.fromDTO(category);

        return ResponseEntity.ok(new ApiResponse(true, "Category retrieved successfully", response));
    }

    /**
     * Create a new category
     *
     * @param request the category creation request
     * @return the created category
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryDTO createdCategory = categoryService.createCategory(request);
        CategoryResponse response = CategoryResponse.fromDTO(createdCategory);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(true, "Category created successfully", response));
    }

    /**
     * Update an existing category
     *
     * @param id the category ID to update
     * @param request the category update request
     * @return the updated category
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long id,
                                                      @Valid @RequestBody CategoryRequest request) {
        CategoryDTO updatedCategory = categoryService.updateCategory(id, request);
        CategoryResponse response = CategoryResponse.fromDTO(updatedCategory);

        return ResponseEntity.ok(new ApiResponse(true, "Category updated successfully", response));
    }

    /**
     * Delete a category by ID
     *
     * @param id the category ID to delete
     * @return success response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ApiResponse(true, "Category deleted successfully"));
    }

    /**
     * Exception handler for category with subcategories deletion attempt
     *
     * @param ex the exception
     * @return error response
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse> handleIllegalStateException(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiResponse(false, ex.getMessage()));
    }
}
