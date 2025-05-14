package com.shopsmart.base.controller;

import com.shopsmart.base.dto.ApiResponse;
import com.shopsmart.base.dto.OrderDTO;
import com.shopsmart.base.model.OrderStatus;
import com.shopsmart.base.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST Controller for handling order operations
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @Autowired
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Get current user's orders with pagination
     *
     * @param principal authenticated user
     * @param pageable pagination information
     * @return page of orders
     */
    @GetMapping("/my-orders")
    public ResponseEntity<ApiResponse> getCurrentUserOrders(
            Principal principal,
            @PageableDefault(size = 10, sort = "orderDate,desc") Pageable pageable) {

        Long userId = getUserIdFromPrincipal(principal);
        Page<OrderDTO> orders = orderService.getUserOrders(userId, pageable);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Orders retrieved successfully",
                orders));
    }

    /**
     * Get order by ID (admin or owner only)
     *
     * @param id order ID
     * @param principal authenticated user
     * @return order details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getOrderById(
            @PathVariable Long id,
            Principal principal) {

        OrderDTO order = orderService.getOrderById(id);

        // Ensure user can only access their own orders (unless admin)
        Long userId = getUserIdFromPrincipal(principal);
        if (!isAdmin(principal) && !order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied", null));
        }

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Order retrieved successfully",
                order));
    }

    /**
     * Get order by order number (admin or owner only)
     *
     * @param orderNumber order number
     * @param principal authenticated user
     * @return order details
     */
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<ApiResponse> getOrderByOrderNumber(
            @PathVariable String orderNumber,
            Principal principal) {

        OrderDTO order = orderService.getOrderByOrderNumber(orderNumber);

        // Ensure user can only access their own orders (unless admin)
        Long userId = getUserIdFromPrincipal(principal);
        if (!isAdmin(principal) && !order.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ApiResponse(false, "Access denied", null));
        }

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Order retrieved successfully",
                order));
    }

    /**
     * Create order from shopping cart
     *
     * @param orderDTO order details
     * @param principal authenticated user
     * @return created order
     */
    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(
            @Valid @RequestBody OrderDTO orderDTO,
            Principal principal) {

        Long userId = getUserIdFromPrincipal(principal);
        OrderDTO createdOrder = orderService.createOrderFromCart(userId, orderDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse(
                        true,
                        "Order created successfully",
                        createdOrder));
    }

    /**
     * Update order status (admin only)
     *
     * @param id order ID
     * @param status new order status
     * @return updated order
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status) {

        OrderDTO updatedOrder = orderService.updateOrderStatus(id, status);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Order status updated successfully",
                updatedOrder));
    }

    /**
     * Get all orders (admin only)
     *
     * @param pageable pagination information
     * @return page of orders
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getAllOrders(
            @PageableDefault(size = 10, sort = "orderDate,desc") Pageable pageable) {

        Page<OrderDTO> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Orders retrieved successfully",
                orders));
    }

    /**
     * Get orders by status (admin only)
     *
     * @param status order status
     * @param pageable pagination information
     * @return page of orders
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getOrdersByStatus(
            @PathVariable OrderStatus status,
            @PageableDefault(size = 10, sort = "orderDate,desc") Pageable pageable) {

        Page<OrderDTO> orders = orderService.getOrdersByStatus(status, pageable);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Orders retrieved successfully",
                orders));
    }

    /**
     * Get orders by date range (admin only)
     *
     * @param startDate start date (format: yyyy-MM-dd)
     * @param endDate end date (format: yyyy-MM-dd)
     * @param pageable pagination information
     * @return page of orders
     */
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getOrdersByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @PageableDefault(size = 10, sort = "orderDate,desc") Pageable pageable) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
        LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");

        Page<OrderDTO> orders = orderService.getOrdersByDateRange(start, end, pageable);

        return ResponseEntity.ok(new ApiResponse(
                true,
                "Orders retrieved successfully",
                orders));
    }

    /**
     * Helper method to extract user ID from Principal
     *
     * @param principal authenticated user
     * @return user ID
     */
    private Long getUserIdFromPrincipal(Principal principal) {
        // Implementation depends on authentication mechanism
        // This is a simplified example
        // In a real application, you might use JWT or Spring Security's UserDetails
        return 1L; // Placeholder for real implementation
    }

    /**
     * Helper method to check if user is admin
     *
     * @param principal authenticated user
     * @return true if admin, false otherwise
     */
    private boolean isAdmin(Principal principal) {
        // Implementation depends on authentication mechanism
        // This is a simplified example
        return false; // Placeholder for real implementation
    }
}
