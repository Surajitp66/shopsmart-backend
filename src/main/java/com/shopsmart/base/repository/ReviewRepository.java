package com.shopsmart.base.repository;

import com.shopsmart.base.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Find all reviews for a specific product with pagination
     *
     * @param productId the product ID
     * @param pageable pagination information
     * @return page of reviews
     */
    Page<Review> findByProductId(Long productId, Pageable pageable);

    /**
     * Find all reviews by a specific user with pagination
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of reviews
     */
    Page<Review> findByUserId(Long userId, Pageable pageable);

    /**
     * Count the number of reviews for a specific product
     *
     * @param productId the product ID
     * @return count of reviews
     */
    Long countByProductId(Long productId);

    /**
     * Check if a user has already reviewed a specific product
     *
     * @param userId the user ID
     * @param productId the product ID
     * @return true if the user has already reviewed the product
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);

    /**
     * Calculate the average rating for a specific product
     *
     * @param productId the product ID
     * @return the average rating
     */
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double calculateAverageRatingByProductId(@Param("productId") Long productId);

    /**
     * Find most recent reviews for a product
     *
     * @param productId the product ID
     * @param pageable pagination information
     * @return page of reviews ordered by creation date
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.createdAt DESC")
    Page<Review> findMostRecentByProductId(@Param("productId") Long productId, Pageable pageable);

    /**
     * Find highest rated reviews for a product
     *
     * @param productId the product ID
     * @param pageable pagination information
     * @return page of reviews ordered by rating
     */
    @Query("SELECT r FROM Review r WHERE r.product.id = :productId ORDER BY r.rating DESC")
    Page<Review> findHighestRatedByProductId(@Param("productId") Long productId, Pageable pageable);
}