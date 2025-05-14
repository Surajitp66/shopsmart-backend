package com.shopsmart.base.repository;

import com.shopsmart.base.model.Order;
import com.shopsmart.base.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find order by order number
     *
     * @param orderNumber the order number
     * @return optional order
     */
    Optional<Order> findByOrderNumber(String orderNumber);

    /**
     * Find orders by user ID with pagination
     *
     * @param userId user ID
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByUserId(Long userId, Pageable pageable);

    /**
     * Find orders by order status with pagination
     *
     * @param orderStatus order status
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);

    /**
     * Find orders by date range with pagination
     *
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find orders by user ID and order status with pagination
     *
     * @param userId user ID
     * @param orderStatus order status
     * @param pageable pagination information
     * @return page of orders
     */
    Page<Order> findByUserIdAndOrderStatus(Long userId, OrderStatus orderStatus, Pageable pageable);

    /**
     * Count orders by order status
     *
     * @param orderStatus order status
     * @return count of orders
     */
    long countByOrderStatus(OrderStatus orderStatus);

    /**
     * Find recent orders for a user
     *
     * @param userId user ID
     * @param limit maximum number of orders to return
     * @return list of recent orders
     */
    @Query(value = "SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC")
    List<Order> findRecentOrdersByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find orders with items containing a specific product
     *
     * @param productId product ID
     * @param pageable pagination information
     * @return page of orders
     */
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.product.id = :productId")
    Page<Order> findOrdersWithProduct(@Param("productId") Long productId, Pageable pageable);
}
