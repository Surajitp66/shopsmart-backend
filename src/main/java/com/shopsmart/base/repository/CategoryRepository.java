package com.shopsmart.base.repository;

import com.shopsmart.base.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    @Query("SELECT c FROM Category c WHERE c.parentCategory IS NULL")
    List<Category> findAllParentCategories();

    List<Category> findByParentCategoryId(Long parentId);

    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.displayOrder")
    List<Category> findAllActiveCategories();

    boolean existsByName(String name);
}
