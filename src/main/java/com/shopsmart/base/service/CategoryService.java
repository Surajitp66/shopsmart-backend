package com.shopsmart.base.service;

import com.shopsmart.base.dto.CategoryDTO;
import com.shopsmart.base.dto.CategoryRequest;
import com.shopsmart.base.dto.CategoryResponse;
import com.shopsmart.base.exception.ResourceAlreadyExistsException;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.model.Category;
import com.shopsmart.base.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling Category-related operations
 */
@Service
public class CategoryService {
    private static final Logger logger = LogManager.getLogger(CategoryService.class);
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * Get all categories
     *
     * @return list of all categories
     */
    public List<CategoryDTO> getAllCategories() {
        logger.info("Fetching all categories");
        return categoryRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all parent categories (categories with no parent)
     *
     * @return list of parent categories
     */
    public List<CategoryDTO> getAllParentCategories() {
        logger.info("Fetching all parent categories");
        return categoryRepository.findAllParentCategories().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all subcategories for a given parent category ID
     *
     * @param parentId the ID of the parent category
     * @return list of subcategories
     */
    public List<CategoryDTO> getSubcategoriesByParentId(Long parentId) {
        logger.info("Fetching subcategories for parent ID: {}", parentId);
        return categoryRepository.findByParentCategoryId(parentId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get a category by ID
     *
     * @param id the category ID
     * @return the category DTO
     * @throws ResourceNotFoundException if category not found
     */
    public CategoryDTO getCategoryById(Long id) {
        logger.info("Fetching category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return convertToDTO(category);
    }

    /**
     * Create a new category
     *
     * @param request the category creation request
     * @return the created category DTO
     * @throws ResourceAlreadyExistsException if category with same name exists
     */
    @Transactional
    public CategoryDTO createCategory(CategoryRequest request) {
        logger.info("Creating new category: {}", request.getName());
        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists with name: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setActive(request.getActive() != null ? request.getActive() : true);
        category.setDisplayOrder(request.getDisplayOrder());

        if (request.getParentCategoryId() != null) {
            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " +
                            request.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        }

        Category savedCategory = categoryRepository.save(category);
        logger.info("Created category with ID: {}", savedCategory.getId());
        return convertToDTO(savedCategory);
    }

    /**
     * Update an existing category
     *
     * @param id the category ID to update
     * @param request the category update request
     * @return the updated category DTO
     * @throws ResourceNotFoundException if category not found
     * @throws ResourceAlreadyExistsException if new name conflicts with existing category
     */
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryRequest request) {
        logger.info("Updating category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new ResourceAlreadyExistsException("Category already exists with name: " + request.getName());
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());

        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        category.setDisplayOrder(request.getDisplayOrder());

        if (request.getParentCategoryId() != null) {
            // Prevent category from being its own parent
            if (request.getParentCategoryId().equals(id)) {
                throw new IllegalArgumentException("Category cannot be its own parent");
            }

            Category parentCategory = categoryRepository.findById(request.getParentCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with id: " +
                            request.getParentCategoryId()));
            category.setParentCategory(parentCategory);
        } else {
            category.setParentCategory(null);
        }

        Category updatedCategory = categoryRepository.save(category);
        logger.info("Updated category with ID: {}", updatedCategory.getId());
        return convertToDTO(updatedCategory);
    }

    /**
     * Delete a category by ID
     *
     * @param id the category ID to delete
     * @throws ResourceNotFoundException if category not found
     * @throws IllegalStateException if category has subcategories
     */
    @Transactional
    public void deleteCategory(Long id) {
        logger.info("Deleting category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        // Check for subcategories and handle them accordingly
        if (!category.getSubCategories().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with subcategories. Remove subcategories first.");
        }

        categoryRepository.delete(category);
        logger.info("Deleted category with ID: {}", id);
    }

    /**
     * Convert a Category entity to CategoryDTO
     *
     * @param category the category entity
     * @return the category DTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        dto.setActive(category.isActive());
        dto.setDisplayOrder(category.getDisplayOrder());

        if (category.getParentCategory() != null) {
            dto.setParentCategoryId(category.getParentCategory().getId());
            dto.setParentCategoryName(category.getParentCategory().getName());
        }

        return dto;
    }
}