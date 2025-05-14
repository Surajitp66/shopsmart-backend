package com.shopsmart.base.service;

import com.shopsmart.base.dto.OrderDTO;
import com.shopsmart.base.dto.OrderItemDTO;
import com.shopsmart.base.exception.InvalidOrderStateException;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.model.*;
import com.shopsmart.base.repository.InventoryRepository;
import com.shopsmart.base.repository.OrderRepository;
import com.shopsmart.base.repository.ProductRepository;
import com.shopsmart.base.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for order management
 */
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final ShoppingCartService shoppingCartService;

    @Autowired
    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        InventoryRepository inventoryRepository,
                        ShoppingCartService shoppingCartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.inventoryRepository = inventoryRepository;
        this.shoppingCartService = shoppingCartService;
    }

    /**
     * Get all orders with pagination (admin only)
     *
     * @param pageable pagination information
     * @return page of orders
     */
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get orders by user ID with pagination
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return page of orders
     */
    public Page<OrderDTO> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get orders by status with pagination
     *
     * @param status   order status
     * @param pageable pagination information
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByOrderStatus(status, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get orders by date range with pagination
     *
     * @param startDate start date (inclusive)
     * @param endDate   end date (inclusive)
     * @param pageable  pagination information
     * @return page of orders
     */
    public Page<OrderDTO> getOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return orderRepository.findByOrderDateBetween(startDate, endDate, pageable)
                .map(this::convertToDTO);
    }

    /**
     * Get order by ID
     *
     * @param id order ID
     * @return order DTO
     * @throws ResourceNotFoundException if order not found
     */
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return convertToDTO(order);
    }

    /**
     * Get order by order number
     *
     * @param orderNumber order number
     * @return order DTO
     * @throws ResourceNotFoundException if order not found
     */
    public OrderDTO getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return convertToDTO(order);
    }

    /**
     * Create order from shopping cart
     *
     * @param userId       user ID
     * @param orderRequest order request data
     * @return created order DTO
     * @throws ResourceNotFoundException if user or product not found
     * @throws IllegalStateException     if cart is empty or inventory is insufficient
     */
    @Transactional
    public OrderDTO createOrderFromCart(Long userId, OrderDTO orderRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Get user's shopping cart
        ShoppingCartDTO cart = shoppingCartService.getCartByUserId(userId);

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Cannot create order from empty cart");
        }

        // Create new order
        Order order = new Order();
        order.setUser(user);
        order.setOrderNumber(generateOrderNumber());
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.PENDING);

        // Set shipping and billing information
        order.setShippingAddress(orderRequest.getShippingAddress());
        order.setBillingAddress(orderRequest.getBillingAddress());
        order.setPaymentMethod(orderRequest.getPaymentMethod());
        order.setNotes(orderRequest.getNotes());

        // Process cart items
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItemDTO cartItem : cart.getItems()) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + cartItem.getProductId()));

            // Check inventory
            Inventory inventory = inventoryRepository.findByProductId(product.getId());
            if (inventory == null || inventory.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Insufficient inventory for product: " + product.getName());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setUnitPrice(product.getPrice());

            // Calculate extended price
            BigDecimal extendedPrice = product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            orderItem.setExtendedPrice(extendedPrice);

            // Add to order
            order.addOrderItem(orderItem);

            // Update inventory
            inventory.setQuantity(inventory.getQuantity() - cartItem.getQuantity());
            inventoryRepository.save(inventory);

            // Add to subtotal
            subtotal = subtotal.add(extendedPrice);
        }

        // Calculate totals
        order.setSubtotal(subtotal);

        // Add shipping cost
        BigDecimal shippingCost = orderRequest.getShippingCost() != null ?
                orderRequest.getShippingCost() : BigDecimal.ZERO;
        order.setShippingCost(shippingCost);

        // Add tax (10% default)
        BigDecimal taxRate = new BigDecimal("0.10");
        BigDecimal taxAmount = subtotal.multiply(taxRate);
        order.setTax(taxAmount);

        // Calculate grand total
        BigDecimal total = subtotal.add(shippingCost).add(taxAmount);
        order.setTotal(total);

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        shoppingCartService.clearCart(userId);

        return convertToDTO(savedOrder);
    }

    /**
     * Update order status
     *
     * @param id        order ID
     * @param newStatus new order status
     * @return updated order DTO
     * @throws ResourceNotFoundException  if order not found
     * @throws InvalidOrderStateException if status transition is invalid
     */
    @Transactional
    public OrderDTO updateOrderStatus(Long id, OrderStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        // Validate status transition
        validateStatusTransition(order.getOrderStatus(), newStatus);

        order.setOrderStatus(newStatus);

        // If order is cancelled, return items to inventory
        if (newStatus == OrderStatus.CANCELLED) {
            restoreInventory(order);
        }

        // If order is delivered, update payment status if not already paid
        if (newStatus == OrderStatus.DELIVERED && order.getPaymentStatus() == PaymentStatus.PENDING) {
            order.setPaymentStatus(PaymentStatus.PAID);
        }

        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    /**
     * Update payment status
     *
     * @param id        order ID
     * @param newStatus new payment status
     * @return updated order DTO
     * @throws ResourceNotFoundException if order not found
     */
    @Transactional
    public OrderDTO updatePaymentStatus(Long id, PaymentStatus newStatus) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));

        order.setPaymentStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    /**
     * Validate order status transition
     *
     * @param currentStatus current order status
     * @param newStatus     new order status
     * @throws InvalidOrderStateException if transition is invalid
     */
    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        // Define valid status transitions
        boolean validTransition = false;

        switch (currentStatus) {
            case PENDING:
                validTransition = newStatus == OrderStatus.PROCESSING ||
                        newStatus == OrderStatus.CANCELLED;
                break;
            case PROCESSING:
                validTransition = newStatus == OrderStatus.SHIPPED ||
                        newStatus == OrderStatus.CANCELLED;
                break;
            case SHIPPED:
                validTransition = newStatus == OrderStatus.DELIVERED ||
                        newStatus == OrderStatus.RETURNED;
                break;
            case DELIVERED:
                validTransition = newStatus == OrderStatus.COMPLETED ||
                        newStatus == OrderStatus.RETURNED;
                break;
            case COMPLETED:
                validTransition = newStatus == OrderStatus.RETURNED;
                break;
            case RETURNED:
            case CANCELLED:
                // Terminal states, no transitions allowed
                validTransition = false;
                break;
        }

        if (!validTransition) {
            throw new InvalidOrderStateException(
                    "Invalid order status transition from " + currentStatus + " to " + newStatus);
        }
    }

    /**
     * Restore inventory for cancelled or returned orders
     *
     * @param order order
     */
    private void restoreInventory(Order order) {
        for (OrderItem item : order.getOrderItems()) {
            Inventory inventory = inventoryRepository.findByProductId(item.getProduct().getId());
            if (inventory != null) {
                inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
                inventoryRepository.save(inventory);
            }
        }
    }

    /**
     * Generate unique order number
     *
     * @return unique order number
     */
    private String generateOrderNumber() {
        // Generate a unique order number
        // Format: Year + Month + Day + Random UUID suffix (first 8 chars)
        LocalDateTime now = LocalDateTime.now();
        String datePart = String.format("%04d%02d%02d", now.getYear(), now.getMonthValue(), now.getDayOfMonth());
        String uuidPart = UUID.randomUUID().toString().substring(0, 8);

        return "ORD-" + datePart + "-" + uuidPart;
    }

    /**
     * Convert Order entity to OrderDTO
     *
     * @param order order entity
     * @return order DTO
     */
    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setUserId(order.getUser().getId());
        dto.setUserEmail(order.getUser().getEmail());
        dto.setOrderDate(order.getOrderDate());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setBillingAddress(order.getBillingAddress());
        dto.setSubtotal(order.getSubtotal());
        dto.setShippingCost(order.getShippingCost());
        dto.setTaxAmount(order.getTax());
        dto.setTotal(order.getTotal());
        dto.setNotes(order.getNotes());

        // Convert order items
        List<OrderItemDTO> itemDTOs = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProduct().getId());
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setUnitPrice(item.getUnitPrice());
                    itemDTO.setExtendedPrice(item.getExtendedPrice());

                    // Add product image if available
                    if (item.getProduct().getImages() != null && !item.getProduct().getImages().isEmpty()) {
                        itemDTO.setProductImageUrl(item.getProduct().getImages().stream()
                                .filter(image -> image.isPrimary())
                                .findFirst()
                                .orElse(item.getProduct().getImages().iterator().next())
                                .getImageUrl());
                    }

                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setItems(itemDTOs);
        dto.setItemCount(itemDTOs.size());

        return dto;
    }
}