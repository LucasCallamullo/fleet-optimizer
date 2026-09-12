package com.fleets.exception;

import java.util.List;

import lombok.Getter;

/**
 * Custom runtime exception for application-specific errors.
 * Allows setting a detail message, an HTTP status code, and an optional
 * list of field-level or item-level error messages.
 *
 * Usage examples:
 * <pre>
 * // Simple error with default status (400)
 * throw new AppException("Invalid license plate format");
 *
 * // Error with explicit status
 * throw new AppException("Vehicle not found", 404);
 *
 * // Error with multiple validation messages
 * throw new AppException("Validation failed", 400, List.of(
 *     "licensePlate: must not be blank",
 *     "capacity: must be greater than 0"
 * ));
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
     * Optional list of field-level or item-level error messages.
     * May be {@code null} when there are no granular errors to report.
     */
    private final List<String> errors;

    /**
     * Constructs a new AppException with a detail message, an HTTP status code,
     * and a list of granular error messages.
     *
     * @param detail     the detail message (returned to the client)
     * @param statusCode the HTTP status code (e.g., 400, 404, 409)
     * @param errors     optional list of field-level error messages; may be {@code null}
     */
    public AppException(String detail, int statusCode, List<String> errors) {
        super(detail);
        this.errors = errors;
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new AppException with a detail message and an HTTP status code.
     * No granular errors are attached.
     *
     * @param detail     the detail message (returned to the client)
     * @param statusCode the HTTP status code (e.g., 400, 404, 409)
     */
    public AppException(String detail, int statusCode) {
        super(detail);
        this.errors = null;
        this.statusCode = statusCode;
    }

    /**
     * Constructs a new AppException with a detail message.
     * Defaults to HTTP 400 (Bad Request) and no granular errors.
     *
     * @param detail the detail message (returned to the client)
     */
    public AppException(String detail) {
        this(detail, 400, null);
    }
}