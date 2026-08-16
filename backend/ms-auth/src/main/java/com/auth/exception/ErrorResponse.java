package com.auth.exception;

import java.time.LocalDateTime;

/**
 * Standard error response format returned to the client when an exception occurs.
 * Provides consistent error structure across all API endpoints.
 * 
 * <p>Response example:
 * <pre>
 * {
 *   "timestamp": "2026-06-08T10:30:00",
 *   "status": 404,
 *   "error": "Vehicle not found with id: 99",
 *   "path": "/api/vehicles/99"
 * }
 * </pre>
 * 
 * @param timestamp the exact time when the error occurred
 * @param status the HTTP status code (e.g., 400, 404, 500)
 * @param error the error message describing what went wrong
 * @param path the request URI that caused the error
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    String message,
    String path,
    boolean success
) {
    
    /**
     * Creates an ErrorResponse with the current timestamp.
     *
     * @param status the HTTP status code
     * @param error the error message
     * @param path the request URI
     */
    public ErrorResponse(int status, String error, String path) {
        this(LocalDateTime.now(), status, error, path, false);
    }
}