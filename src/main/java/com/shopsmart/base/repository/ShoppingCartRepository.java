package com.shopsmart.base.repository;

import com.shopsmart.base.model.ShoppingCart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ShoppingCart entity
 */
@Repository
public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {

    /**
     * Find cart by user ID
     *
     * @param userId the user ID
     * @return optional cart
     */
    Optional<ShoppingCart> findByUserId(Long userId);

    /**
     * Find cart by session ID
     *
     * @param sessionId the session ID
     * @return optional cart
     */
    Optional<ShoppingCart> findBySessionId(String sessionId);

    /**
     * Find carts that have expired before the given date
     *
     * @param date the date to check expiry against
     * @return list of expired carts
     */
    List<ShoppingCart> findByExpiresAtBefore(LocalDateTime date);

    /**
     * Delete carts by session ID
     *
     * @param sessionId the session ID
     */
    void deleteBySessionId(String sessionId);
}