package com.shopsmart.base.repository;

import com.shopsmart.base.model.StockMovement;
import com.shopsmart.base.model.StockMovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for StockMovement entity
 */
@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    /**
     * Find stock movements by product ID ordered by movement date descending
     *
     * @param productId the product ID
     * @return list of stock movements for the product
     */
    List<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId);

    /**
     * Find stock movements by product ID with pagination
     *
     * @param productId the product ID
     * @param pageable pagination information
     * @return page of stock movements for the product
     */
    Page<StockMovement> findByProductId(Long productId, Pageable pageable);

    /**
     * Find stock movements by movement type
     *
     * @param movementType the movement type
     * @param pageable pagination information
     * @return page of stock movements of the specified type
     */
    Page<StockMovement> findByMovementType(StockMovementType movementType, Pageable pageable);

    /**
     * Find stock movements within a date range
     *
     * @param startDate the start date
     * @param endDate the end date
     * @param pageable pagination information
     * @return page of stock movements within the date range
     */
    @Query("SELECT sm FROM StockMovement sm WHERE sm.movementDate BETWEEN :startDate AND :endDate")
    Page<StockMovement> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    /**
     * Find stock movements by reference
     *
     * @param reference the reference
     * @return list of stock movements with the specified reference
     */
    List<StockMovement> findByReferenceContainingIgnoreCase(String reference);
}
