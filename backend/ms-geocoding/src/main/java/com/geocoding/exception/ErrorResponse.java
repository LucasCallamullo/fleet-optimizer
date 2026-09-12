package com.geocoding.exception;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response format returned to the client when an exception occurs.
 * Provides consistent error structure across all API endpoints.
 *
 * @param timestamp the exact time when the error occurred
 * @param status the HTTP status code (e.g., 400, 404, 500)
 * @param path the request URI that caused the error
 * @param success always false for error responses
 * @param detail a human-readable summary of what went wrong
 * @param errors optional list of field-level or item-level error messages
 */
public record ErrorResponse(
    LocalDateTime timestamp,
    int status,
    boolean success,
    String detail,
    List<String> errors,
    String path
) {

    /**
     * Creates an ErrorResponse with the current timestamp and no field-level errors.
     *
     * @param status the HTTP status code
     * @param detail the error message
     * @param path the request URI
     */
    public ErrorResponse(int status, String detail, String path) {
        this(LocalDateTime.now(), status, false, detail, null, path);
    }

    /**
     * Creates an ErrorResponse with the current timestamp and a list of field-level errors.
     *
     * @param status the HTTP status code
     * @param detail a summary of the error
     * @param path the request URI
     * @param errors list of field-level error messages
     */
    public ErrorResponse(int status, String detail, String path, List<String> errors) {
        this(LocalDateTime.now(), status, false, detail, errors, path);
    }
}