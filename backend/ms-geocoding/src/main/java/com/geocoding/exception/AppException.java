package com.geocoding.exception;

import java.util.List;

import lombok.Getter;

/**
 * Custom runtime exception for application-specific errors.
 * Allows setting both a message and an HTTP status code.
 * 
 * @see GlobalExceptionHandler
 */
@Getter
public class AppException extends RuntimeException {
    
    /**
     * HTTP status code to be returned to the client.
     */
    private final int statusCode;
    private final List<String> errors;
    
    /**
     * Constructs a new AppException with the specified message and status code.
     *
     * @param detail the detail message (returned to client)
     * @param statusCode the HTTP status code (e.g., 400, 404, 409)
     */
    public AppException(String detail, int statusCode) {
        super(detail);
        this.statusCode = statusCode;
        this.errors = null;
    }

    public AppException(String detail, int statusCode, List<String> errors) {
        super(detail);
        this.statusCode = statusCode;
        this.errors = errors;
    }
    
    /**
     * Constructs a new AppException with the specified message.
     * Default status code is 400 (Bad Request).
     *
     * @param detail the detail message (returned to client)
     */
    public AppException(String detail) {
        this(detail, 400);
    }
}