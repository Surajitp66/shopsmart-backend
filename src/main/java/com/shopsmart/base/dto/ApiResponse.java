package com.shopsmart.base.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
/**
        * Generic API response class for consistent API responses
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private Boolean success;
    private String message;
    private Object data;

    // Default constructor
    public ApiResponse() {
    }

    /**
     * Constructor with success and message
     *
     * @param success whether the operation was successful
     * @param message response message
     */
    public ApiResponse(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * Constructor with success, message, and data
     *
     * @param success whether the operation was successful
     * @param message response message
     * @param data response data
     */
    public ApiResponse(Boolean success, String message, Object data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    // Getters and Setters
    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}
