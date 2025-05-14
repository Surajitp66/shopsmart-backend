package com.shopsmart.base.repository;

import com.shopsmart.base.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;


/**
 * Repository interface for Product entities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find a product by its SKU
     *
     * @param sku product SKU
     * @return product if found
     */
    Optional<Product> findBySku(String sku);

    /**
     * Find products by category id
     *
     * @param categoryId category identifier
     * @param pageable pagination information
     * @return page of products
     */
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find products by active status
     *
     * @param active active status
     * @param pageable pagination information
     * @return page of products
     */
    Page<Product> findByActive(boolean active, Pageable pageable);

    /**
     * Find featured products
     *
     * @param pageable pagination information
     * @return page of featured products
     */
    @Query("SELECT p FROM Product p WHERE p.featured = true AND p.active = true ORDER BY p.createdAt DESC")
    Page<Product> findFeaturedProducts(Pageable pageable);

    /**
     * Find products by price range
     *
     * @param minPrice minimum price
     * @param maxPrice maximum price
     * @param pageable pagination information
     * @return page of products
     */
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.active = true")
    Page<Product> findByPriceRange(
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    /**
     * Search products by keyword in name or description
     *
     * @param keyword search keyword
     * @param pageable pagination information
     * @return page of products
     */
    @Query("SELECT p FROM Product p WHERE " +
            "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.sku) = LOWER(:keyword)) AND p.active = true")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find products by tag name
     *
     * @param tagName tag name
     * @param pageable pagination information
     * @return page of products
     */
    @Query("SELECT p FROM Product p JOIN p.attributes a WHERE " +
            "LOWER(a.name) = LOWER(:tagName) AND p.active = true")
    Page<Product> findByTagName(@Param("tagName") String tagName, Pageable pageable);

    /**
     * Find products that have low inventory
     *
     * @param threshold inventory threshold
     * @param pageable pagination information
     * @return page of products
     */
    @Query("SELECT p FROM Product p JOIN p.inventory i WHERE " +
            "i.quantity <= :threshold AND p.active = true")
    Page<Product> findProductsWithLowInventory(@Param("threshold") int threshold, Pageable pageable);
}