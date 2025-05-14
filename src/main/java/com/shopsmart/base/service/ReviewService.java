package com.shopsmart.base.service;

import com.shopsmart.base.dto.ReviewDTO;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.model.Product;
import com.shopsmart.base.model.Review;
import com.shopsmart.base.model.User;
import com.shopsmart.base.repository.ProductRepository;
import com.shopsmart.base.repository.ReviewRepository;
import com.shopsmart.base.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    /**
     * Create a new review for a product
     *
     * @param reviewDTO the review data
     * @param userId the ID of the user creating the review
     * @return the created review as DTO
     */
    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Product product = productRepository.findById(reviewDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + reviewDTO.getProductId()));

        // Check if user has already reviewed this product
        if (reviewRepository.existsByUserIdAndProductId(userId, product.getId())) {
            throw new IllegalStateException("User has already reviewed this product");
        }

        Review review = new Review();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setUser(user);
        review.setProduct(product);
        review.setCreatedAt(LocalDateTime.now());
        review.setUpdatedAt(LocalDateTime.now());

        Review savedReview = reviewRepository.save(review);

        // Update product average rating
        updateProductAverageRating(product.getId());

        return convertToDTO(savedReview);
    }

    /**
     * Get all reviews for a specific product
     *
     * @param productId the product ID
     * @param pageable pagination information
     * @return page of review DTOs
     */
    public Page<ReviewDTO> getReviewsByProduct(Long productId, Pageable pageable) {
        // Check if product exists
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Page<Review> reviews = reviewRepository.findByProductId(productId, pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Get all reviews by a specific user
     *
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of review DTOs
     */
    public Page<ReviewDTO> getReviewsByUser(Long userId, Pageable pageable) {
        // Check if user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }

        Page<Review> reviews = reviewRepository.findByUserId(userId, pageable);
        return reviews.map(this::convertToDTO);
    }

    /**
     * Get a specific review by ID
     *
     * @param reviewId the review ID
     * @return the review DTO
     */
    public ReviewDTO getReviewById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        return convertToDTO(review);
    }

    /**
     * Update an existing review
     *
     * @param reviewId the review ID to update
     * @param reviewDTO the updated review data
     * @param userId the ID of the user making the update
     * @return the updated review as DTO
     */
    @Transactional
    public ReviewDTO updateReview(Long reviewId, ReviewDTO reviewDTO, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Check if user is authorized to update this review
        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("User is not authorized to update this review");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setUpdatedAt(LocalDateTime.now());

        Review updatedReview = reviewRepository.save(review);

        // Update product average rating
        updateProductAverageRating(review.getProduct().getId());

        return convertToDTO(updatedReview);
    }

    /**
     * Delete a review
     *
     * @param reviewId the review ID to delete
     * @param userId the ID of the user making the deletion request
     */
    @Transactional
    public void deleteReview(Long reviewId, Long userId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + reviewId));

        // Check if user is authorized to delete this review
        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("User is not authorized to delete this review");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        // Update product average rating after deletion
        updateProductAverageRating(productId);
    }

    /**
     * Update the average rating for a product
     *
     * @param productId the product ID
     */
    private void updateProductAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Double avgRating = reviewRepository.calculateAverageRatingByProductId(productId);
        product.setAverageRating(avgRating != null ? avgRating : 0.0);
        product.setReviewCount(reviewRepository.countByProductId(productId));

        productRepository.save(product);
    }

    /**
     * Convert a Review entity to ReviewDTO
     *
     * @param review the review entity
     * @return the review DTO
     */
    private ReviewDTO convertToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setProductId(review.getProduct().getId());
        dto.setUserId(review.getUser().getId());
        dto.setUsername(review.getUser().getUsername());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setUpdatedAt(review.getUpdatedAt());
        return dto;
    }

    /**
     * Get the count of reviews for a product
     *
     * @param productId the product ID
     * @return number of reviews
     */
    public Long countReviewsByProduct(Long productId) {
        return reviewRepository.countByProductId(productId);
    }
}