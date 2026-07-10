package com.packages.exception;

import lombok.Getter;

/**
 * Custom runtime exception for application-specific errors.
 * Allows setting both a message and an HTTP status code.
 * 
 * <p>Usage example:
 * <pre>
 * throw new AppException("Vehicle not found", 404);
 * throw new AppException("Invalid license plate format");
 * </pre>
 * 
 * @see GlobalExceptionHandler
 */
@Getter
public class AppException extends RuntimeException {
    
    /**
     * HTTP status code to be returned to the client.
     */
    private final int statusCode;
    
    /**
     * Constructs a new AppException with the specified message and status code.
     *
     * @param message the detail message (returned to client)
     * @param statusCode the HTTP status code (e.g., 400, 404, 409)
     */
    public AppException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    /**
     * Constructs a new AppException with the specified message.
     * Default status code is 400 (Bad Request).
     *
     * @param message the detail message (returned to client)
     */
    public AppException(String message) {
        this(message, 400);
    }
}
