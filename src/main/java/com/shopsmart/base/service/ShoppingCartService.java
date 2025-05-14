package com.shopsmart.base.service;

import com.shopsmart.base.dto.CartItemDTO;
import com.shopsmart.base.dto.ShoppingCartDTO;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.model.*;
import com.shopsmart.base.repository.ProductRepository;
import com.shopsmart.base.repository.ShoppingCartRepository;
import com.shopsmart.base.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {

    private final ShoppingCartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Autowired
    public ShoppingCartService(ShoppingCartRepository cartRepository,
                               UserRepository userRepository,
                               ProductRepository productRepository) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public ShoppingCartDTO getCartByUserId(Long userId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    ShoppingCart newCart = new ShoppingCart(user);
                    return cartRepository.save(newCart);
                });

        return convertToDTO(cart);
    }

    public ShoppingCartDTO getCartBySessionId(String sessionId) {
        ShoppingCart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart(sessionId);
                    return cartRepository.save(newCart);
                });

        return convertToDTO(cart);
    }

    @Transactional
    public ShoppingCartDTO addItemToCart(Long userId, Long productId, Integer quantity) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                    ShoppingCart newCart = new ShoppingCart(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            existingItem.get().updateQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            // Add new item
            CartItem cartItem = new CartItem(cart, product, quantity, product.getPrice());
            cart.addCartItem(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        ShoppingCart updatedCart = cartRepository.save(cart);

        return convertToDTO(updatedCart);
    }

    @Transactional
    public ShoppingCartDTO addItemToCartBySession(String sessionId, Long productId, Integer quantity) {
        ShoppingCart cart = cartRepository.findBySessionId(sessionId)
                .orElseGet(() -> {
                    ShoppingCart newCart = new ShoppingCart(sessionId);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        // Check if product already in cart
        Optional<CartItem> existingItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            // Update quantity
            existingItem.get().updateQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            // Add new item
            CartItem cartItem = new CartItem(cart, product, quantity, product.getPrice());
            cart.addCartItem(cartItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        ShoppingCart updatedCart = cartRepository.save(cart);

        return convertToDTO(updatedCart);
    }

    @Transactional
    public ShoppingCartDTO updateCartItemQuantity(Long userId, Long cartItemId, Integer quantity) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        if (quantity <= 0) {
            cart.removeCartItem(cartItem);
        } else {
            cartItem.updateQuantity(quantity);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        ShoppingCart updatedCart = cartRepository.save(cart);

        return convertToDTO(updatedCart);
    }

    @Transactional
    public ShoppingCartDTO removeCartItem(Long userId, Long cartItemId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));

        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + cartItemId));

        cart.removeCartItem(cartItem);
        cart.setUpdatedAt(LocalDateTime.now());
        ShoppingCart updatedCart = cartRepository.save(cart);

        return convertToDTO(updatedCart);
    }

    @Transactional
    public void clearCart(Long userId) {
        ShoppingCart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user id: " + userId));

        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());
        cartRepository.save(cart);
    }

    @Transactional
    public void mergeGuestCartWithUserCart(String sessionId, Long userId) {
        ShoppingCart guestCart = cartRepository.findBySessionId(sessionId)
                .orElse(null);

        if (guestCart != null && !guestCart.getCartItems().isEmpty()) {
            ShoppingCart userCart = cartRepository.findByUserId(userId)
                    .orElseGet(() -> {
                        User user = userRepository.findById(userId)
                                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                        ShoppingCart newCart = new ShoppingCart(user);
                        return cartRepository.save(newCart);
                    });

            // Merge items from guest cart to user cart
            for (CartItem guestItem : guestCart.getCartItems()) {
                Optional<CartItem> existingItem = userCart.getCartItems().stream()
                        .filter(item -> item.getProduct().getId().equals(guestItem.getProduct().getId()))
                        .findFirst();

                if (existingItem.isPresent()) {
                    // Update quantity if item exists
                    existingItem.get().updateQuantity(existingItem.get().getQuantity() + guestItem.getQuantity());

                } else {
                    // Add new item if not exists
                    CartItem userCartItem = new CartItem(userCart, guestItem.getProduct(),
                            guestItem.getQuantity(), guestItem.getUnitPrice());
                    userCart.addCartItem(userCartItem);
                }
            }

            userCart.setUpdatedAt(LocalDateTime.now());
            cartRepository.save(userCart);

            // Delete the guest cart
            cartRepository.delete(guestCart);
        }
    }

    @Transactional
    public void cleanupExpiredCarts() {
        LocalDateTime expiryTime = LocalDateTime.now().minusDays(7); // Carts older than 7 days
        List<ShoppingCart> expiredCarts = cartRepository.findByExpiresAtBefore(expiryTime);
        cartRepository.deleteAll(expiredCarts);
    }

    private ShoppingCartDTO convertToDTO(ShoppingCart cart) {
        ShoppingCartDTO dto = new ShoppingCartDTO();
        dto.setId(cart.getId());

        if (cart.getUser() != null) {
            dto.setUserId(cart.getUser().getId());
        }

        dto.setSessionId(cart.getSessionId());
        dto.setCreatedAt(cart.getCreatedAt());
        dto.setUpdatedAt(cart.getUpdatedAt());

        // Convert cart items
        dto.setItems(cart.getCartItems().stream()
                .map(this::convertCartItemToDTO)
                .collect(Collectors.toList()));

        // Calculate totals
        dto.setItemCount(cart.getCartItems().size());
        dto.setTotalQuantity(cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum());

        BigDecimal subtotal = cart.getCartItems().stream()
                .map(item -> item.getUnitPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setSubtotal(subtotal);

        // Apply any discounts, shipping costs, etc.
        // For now, just set the total the same as subtotal
        dto.setTotal(subtotal);

        return dto;
    }

    private CartItemDTO convertCartItemToDTO(CartItem item) {
        CartItemDTO dto = new CartItemDTO();
        dto.setId(item.getId());
        dto.setProductId(item.getProduct().getId());
        dto.setProductName(item.getProduct().getName());
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());

        // Calculate extended price
        BigDecimal extendedPrice = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
        dto.setExtendedPrice(extendedPrice);

        // Add product image if available
        if (!item.getProduct().getImages().isEmpty()) {
            dto.setProductImageUrl(item.getProduct().getImages().stream()
                    .filter(ProductImage::isPrimary)
                    .findFirst()
                    .orElse(item.getProduct().getImages().iterator().next())
                    .getImageUrl());
        }

        return dto;
    }
}