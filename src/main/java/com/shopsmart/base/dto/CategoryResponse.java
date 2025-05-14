package com.shopsmart.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for Category data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private boolean active;
    private Integer displayOrder;
    private Long parentCategoryId;
    private String parentCategoryName;

    // Default constructor
    public CategoryResponse() {
    }

    // Constructor with essential fields
    public CategoryResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Full constructor
    public CategoryResponse(Long id, String name, String description, String imageUrl,
                            boolean active, Integer displayOrder, Long parentCategoryId, String parentCategoryName) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.active = active;
        this.displayOrder = displayOrder;
        this.parentCategoryId = parentCategoryId;
        this.parentCategoryName = parentCategoryName;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getParentCategoryId() {
        return parentCategoryId;
    }

    public void setParentCategoryId(Long parentCategoryId) {
        this.parentCategoryId = parentCategoryId;
    }

    public String getParentCategoryName() {
        return parentCategoryName;
    }

    public void setParentCategoryName(String parentCategoryName) {
        this.parentCategoryName = parentCategoryName;
    }

    /**
     * Create a CategoryResponse from a CategoryDTO
     *
     * @param dto the CategoryDTO to convert
     * @return a new CategoryResponse instance
     */
    public static CategoryResponse fromDTO(CategoryDTO dto) {
        return new CategoryResponse(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getImageUrl(),
                dto.isActive(),
                dto.getDisplayOrder(),
                dto.getParentCategoryId(),
                dto.getParentCategoryName()
        );
    }

    @Override
    public String toString() {
        return "CategoryResponse{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", active=" + active +
                ", displayOrder=" + displayOrder +
                ", parentCategoryId=" + parentCategoryId +
                ", parentCategoryName='" + parentCategoryName + '\'' +
                '}';
    }
}