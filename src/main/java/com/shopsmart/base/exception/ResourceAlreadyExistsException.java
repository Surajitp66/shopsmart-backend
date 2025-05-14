package com.shopsmart.base.exception;

/**
 * Exception for resource already exists scenarios
 */
public class ResourceAlreadyExistsException extends RuntimeException {
    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}