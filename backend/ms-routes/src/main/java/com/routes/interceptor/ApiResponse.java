package com.routes.interceptor;

import java.time.LocalDateTime;

/**
 * Standard API response format for successful responses.
 * Wraps the actual data with metadata.
 * 
 * @param timestamp when the response was generated
 * @param status HTTP status code
 * @param message human-readable message
 * @param data the actual response data
 * @param success always true for successful responses
 */
public record ApiResponse<T>(
    LocalDateTime timestamp,
    int status,
    boolean success,
    T data
) {
    /**
     * Creates a success response with the current timestamp.
     * 
     * @param data the actual data
     */
    public ApiResponse(T data) {
        this(LocalDateTime.now(), 200, true, data);
    }

    /**
     * Creates a success response with custom status code.
     * 
     * @param status HTTP status code
     * @param detail success message
     * @param data the actual data
     */
    public ApiResponse(int status, T data) {
        this(LocalDateTime.now(), status, true, data);
    }
}


