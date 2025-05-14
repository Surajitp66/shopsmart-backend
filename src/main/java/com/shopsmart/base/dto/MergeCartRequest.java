package com.shopsmart.base.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for merging guest cart with user cart
 */
public class MergeCartRequest {

    @NotBlank(message = "Session ID cannot be blank")
    private String sessionId;

    // Default constructor
    public MergeCartRequest() {
    }

    // Constructor with parameters
    public MergeCartRequest(String sessionId) {
        this.sessionId = sessionId;
    }

    // Getters and Setters
    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "MergeCartRequest{" +
                "sessionId='" + sessionId + '\'' +
                '}';
    }
}
