package com.shopsmart.base.repository;

import com.shopsmart.base.model.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {

    Optional<Discount> findByCode(String code);

    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :now AND d.endDate >= :now")
    List<Discount> findActiveDiscounts(@Param("now") LocalDateTime now);

    @Query("SELECT d FROM Discount d WHERE d.isActive = true AND d.startDate <= :now AND d.endDate >= :now " +
            "AND (d.usageLimit IS NULL OR d.usageCount < d.usageLimit)")
    List<Discount> findValidDiscounts(@Param("now") LocalDateTime now);

    @Query(value = "SELECT d.* FROM discounts d " +
            "INNER JOIN discount_categories dc ON d.id = dc.discount_id " +
            "WHERE dc.category_id = :categoryId AND d.is_active = true " +
            "AND d.start_date <= :now AND d.end_date >= :now",
            nativeQuery = true)
    List<Discount> findActiveByCategoryId(@Param("categoryId") Long categoryId, @Param("now") LocalDateTime now);
}
