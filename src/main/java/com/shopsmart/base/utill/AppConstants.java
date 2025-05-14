package com.shopsmart.base.utill;

/**
 * Application constants class
 */
public class AppConstants {

    /**
     * Default page number for pagination
     */
    public static final String DEFAULT_PAGE_NUMBER = "0";

    /**
     * Default page size for pagination
     */
    public static final String DEFAULT_PAGE_SIZE = "10";

    /**
     * Maximum page size allowed for pagination
     */
    public static final int MAX_PAGE_SIZE = 50;

    /**
     * Default sort direction
     */
    public static final String DEFAULT_SORT_DIRECTION = "desc";

    /**
     * User role constants
     */
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";

    /**
     * Minimum password length
     */
    public static final int MIN_PASSWORD_LENGTH = 8;

    /**
     * JWT token expiration time in milliseconds (24 hours)
     */
    public static final long JWT_EXPIRATION_TIME = 86400000; // 24 hours

    /**
     * Private constructor to prevent instantiation
     */
    private AppConstants() {
        // Private constructor to prevent instantiation
    }
}