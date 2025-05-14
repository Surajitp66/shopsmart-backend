package com.shopsmart.base.controller;

import com.shopsmart.base.dto.*;
import com.shopsmart.base.exception.ResourceNotFoundException;
import com.shopsmart.base.service.ShoppingCartService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for shopping cart operations
 */
@RestController
@RequestMapping("/api/cart")
public class ShoppingCartController {

    private static final Logger logger = LoggerFactory.getLogger(ShoppingCartController.class);

    private final ShoppingCartService cartService;

    @Autowired
    public ShoppingCartController(ShoppingCartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Get authenticated user's cart
     *
     * @param userDetails the authenticated user
     * @return the cart
     */
    @GetMapping
    public ResponseEntity<ApiResponse> getCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Assuming UserDetails has a method to get userId or we derive it from username
            Long userId = getUserId(userDetails);
            ShoppingCartDTO cart = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Cart retrieved successfully", cart));
        } catch (Exception e) {
            logger.error("Error retrieving cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving cart: " + e.getMessage()));
        }
    }

    /**
     * Get guest cart by session ID
     *
     * @param sessionId the session ID
     * @return the cart
     */
    @GetMapping("/guest/{sessionId}")
    public ResponseEntity<ApiResponse> getGuestCart(@PathVariable String sessionId) {
        try {
            ShoppingCartDTO cart = cartService.getCartBySessionId(sessionId);
            return ResponseEntity.ok(new ApiResponse(true, "Guest cart retrieved successfully", cart));
        } catch (Exception e) {
            logger.error("Error retrieving guest cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving guest cart: " + e.getMessage()));
        }
    }

    /**
     * Add item to authenticated user's cart
     *
     * @param userDetails the authenticated user
     * @param request the add to cart request
     * @return the updated cart
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse> addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AddToCartRequest request) {
        try {
            Long userId = getUserId(userDetails);
            ShoppingCartDTO updatedCart = cartService.addItemToCart(
                    userId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(new ApiResponse(true, "Item added to cart", updatedCart));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding item to cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error adding item to cart: " + e.getMessage()));
        }
    }

    /**
     * Add item to guest cart
     *
     * @param sessionId the session ID
     * @param request the add to cart request
     * @return the updated cart
     */
    @PostMapping("/guest/{sessionId}/items")
    public ResponseEntity<ApiResponse> addToGuestCart(
            @PathVariable String sessionId,
            @Valid @RequestBody AddToCartRequest request) {
        try {
            ShoppingCartDTO updatedCart = cartService.addItemToCartBySession(
                    sessionId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(new ApiResponse(true, "Item added to guest cart", updatedCart));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error adding item to guest cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error adding item to guest cart: " + e.getMessage()));
        }
    }

    /**
     * Update cart item quantity
     *
     * @param userDetails the authenticated user
     * @param cartItemId the cart item ID
     * @param request the update request
     * @return the updated cart
     */
    @PutMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse> updateCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        try {
            Long userId = getUserId(userDetails);
            ShoppingCartDTO updatedCart = cartService.updateCartItemQuantity(
                    userId, cartItemId, request.getQuantity());
            return ResponseEntity.ok(new ApiResponse(true, "Cart item updated", updatedCart));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error updating cart item", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error updating cart item: " + e.getMessage()));
        }
    }

    /**
     * Remove item from cart
     *
     * @param userDetails the authenticated user
     * @param cartItemId the cart item ID
     * @return the updated cart
     */
    @DeleteMapping("/items/{cartItemId}")
    public ResponseEntity<ApiResponse> removeCartItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long cartItemId) {
        try {
            Long userId = getUserId(userDetails);
            ShoppingCartDTO updatedCart = cartService.removeCartItem(userId, cartItemId);
            return ResponseEntity.ok(new ApiResponse(true, "Item removed from cart", updatedCart));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error removing item from cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error removing item from cart: " + e.getMessage()));
        }
    }

    /**
     * Clear user's cart
     *
     * @param userDetails the authenticated user
     * @return success response
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse> clearCart(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            Long userId = getUserId(userDetails);
            cartService.clearCart(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Cart cleared successfully"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error clearing cart", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error clearing cart: " + e.getMessage()));
        }
    }

    /**
     * Merge guest cart with user cart
     *
     * @param userDetails the authenticated user
     * @param request the merge request
     * @return success response
     */
    @PostMapping("/merge")
    public ResponseEntity<ApiResponse> mergeCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MergeCartRequest request) {
        try {
            Long userId = getUserId(userDetails);
            cartService.mergeGuestCartWithUserCart(request.getSessionId(), userId);
            ShoppingCartDTO updatedCart = cartService.getCartByUserId(userId);
            return ResponseEntity.ok(new ApiResponse(true, "Carts merged successfully", updatedCart));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error merging carts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error merging carts: " + e.getMessage()));
        }
    }

    /**
     * Helper method to extract user ID from UserDetails
     * This would depend on your actual implementation of UserDetails
     *
     * @param userDetails the user details
     * @return the user ID
     */
    private Long getUserId(UserDetails userDetails) {
        // Implementation depends on how you store user ID in UserDetails
        // This is a placeholder - you'll need to implement based on your user structure
        if (userDetails instanceof CustomUserDetails) {
            return ((CustomUserDetails) userDetails).getId();
        }

        // Fallback implementation - get user by username
        // You might need a UserService to look up the ID by username
        // For now, throwing an exception
        throw new IllegalStateException("Unable to determine user ID from security context");
    }

    /**
     * Interface to represent custom user details with ID
     * Replace this with your actual implementation
     */
    private interface CustomUserDetails extends UserDetails {
        Long getId();
    }
}
