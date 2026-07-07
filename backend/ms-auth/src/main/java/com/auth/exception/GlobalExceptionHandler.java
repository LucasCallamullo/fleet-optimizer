package com.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.util.stream.Collectors;

/**
 * Global exception handler for reactive applications (WebFlux).
 * 
 * This handler intercepts exceptions thrown from controllers/services
 * and transforms them into consistent ErrorResponse format.
 * 
 * For reactive applications, we use ServerWebExchange instead of HttpServletRequest.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Spring Security AccessDeniedException (@PreAuthorize / roles).
     * Returns 403 Forbidden.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAccessDenied(
            AccessDeniedException ex,
            ServerWebExchange exchange) {
        
        String path = exchange.getRequest().getURI().getPath();
        
        ErrorResponse error = new ErrorResponse(
            403,
            "Forbidden: " + ex.getMessage(), // Esto devolverá "Forbidden: Access Denied"
            path
        );
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(error));
    }

    /**
     * Handles Spring Security AuthenticationException (Missing or invalid auth context).
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAuthenticationException(
            AuthenticationException ex,
            ServerWebExchange exchange) {
        
        String path = exchange.getRequest().getURI().getPath();
        
        ErrorResponse error = new ErrorResponse(
            401,
            "Unauthorized: " + ex.getMessage(),
            path
        );
        
        return Mono.just(ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(error));
    }

    /**
     * Handles custom AppException.
     * 
     * Step-by-step:
     * 1. Extract the request path from the exchange
     * 2. Build ErrorResponse with status code and message
     * 3. Wrap in Mono and return as ResponseEntity
     */
    @ExceptionHandler(AppException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleAppException(
            AppException ex,
            ServerWebExchange exchange) {

        // Step 1: Extract request path for error context
        String path = exchange.getRequest().getURI().getPath();
        
        // Step 2: Build error response
        ErrorResponse error = new ErrorResponse(
            ex.getStatusCode(),
            ex.getMessage(),
            path
        );
        
        // Step 3: Return as ResponseEntity with proper status
        return Mono.just(ResponseEntity
            .status(ex.getStatusCode())
            .body(error));
    }

    /**
     * Handles validation errors from @Valid.
     * 
     * Step-by-step:
     * 1. Extract the request path
     * 2. Collect all validation error messages into a single string
     * 3. Build ErrorResponse with 400 status
     * 4. Return as ResponseEntity
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationExceptions(
            WebExchangeBindException ex,
            ServerWebExchange exchange) {

        // Step 1: Extract request path
        String path = exchange.getRequest().getURI().getPath();
        
        // Step 2: Collect all validation error messages
        // Example: "Email is required, Password must be at least 6 characters"
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        // Step 3: Build error response with 400 status
        ErrorResponse error = new ErrorResponse(
            400,
            errorMessage,
            path
        );
        
        // Step 4: Return as ResponseEntity
        return Mono.just(ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(error));
    }

    /**
     * Handles generic exceptions (fallback for any unhandled exception).
     * 
     * Step-by-step:
     * 1. Extract the request path
     * 2. Build ErrorResponse with 500 status
     * 3. Return as ResponseEntity
     * 
     * Note: This is a safety net. Specific exceptions should be handled
     * by dedicated handlers above for better error messages.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGeneric(
            Exception ex,
            ServerWebExchange exchange) {

        // Step 1: Extract request path
        String path = exchange.getRequest().getURI().getPath();

        // Step 2: Build generic error response with 500 status
        ErrorResponse error = new ErrorResponse(
            500,
            "Internal server error: " + ex.getMessage(),
            path
        );
        
        // Step 3: Return as ResponseEntity
        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(error));
    }
}