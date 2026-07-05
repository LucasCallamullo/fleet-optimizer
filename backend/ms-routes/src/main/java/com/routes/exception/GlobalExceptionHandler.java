package com.routes.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Global exception handler that acts as centralized error-handling middleware.
 * Similar to Express.js error-handling middleware (app.use((err, req, res, next) => {...})).
 * 
 * <p>This class intercepts exceptions thrown from any controller or service
 * and transforms them into a consistent ErrorResponse format.
 * 
 * <p><strong>Analogy with Express.js:</strong>
 * <pre>
 * // Express.js middleware
 * app.use((err, req, res, next) => {
 *   res.status(err.statusCode || 500).json({
 *     timestamp: new Date(),
 *     status: err.statusCode,
 *     error: err.message
 *   });
 * });
 * </pre>
 * 
 * <p><strong>Spring Boot equivalent:</strong>
 * <pre>
 * &#64;ExceptionHandler(AppException.class)
 * public ResponseEntity&lt;ErrorResponse&gt; handleAppException(AppException ex, HttpServletRequest request) {
 *     return buildErrorResponse(ex.getMessage(), ex.getStatusCode(), request);
 * }
 * </pre>
 * 
 * @see AppException
 * @see ErrorResponse
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    

    // ============================================
    // VALIDATION ERRORS (from @Valid in controllers)
    // ============================================
    
    /**
     * Handles validation errors from @Valid annotated DTOs.
     * Triggered when request body fails validation constraints.
     * 
     * <p>Example: POST /api/vehicles with invalid license plate format
     * 
     * @param ex the MethodArgumentNotValidException containing validation errors
     * @param request the HTTP request
     * @return ErrorResponse with 400 status and detailed validation messages
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Validation error at {}: {}", request.getRequestURI(), errorMessage);
        
        ErrorResponse error = new ErrorResponse(400, errorMessage, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles validation errors for path variables and query parameters.
     * Example: @Min(1) Long id in path parameter with value 0.
     * 
     * @param ex the ConstraintViolationException
     * @param request the HTTP request
     * @return ErrorResponse with 400 status
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {
        
        String errorMessage = ex.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .collect(Collectors.joining(", "));
        
        log.warn("Constraint violation at {}: {}", request.getRequestURI(), errorMessage);
        
        ErrorResponse error = new ErrorResponse(400, errorMessage, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handles type mismatch errors (e.g., String passed to Long parameter).
     * Example: GET /api/vehicles/abc (where id should be a number)
     * 
     * @param ex the MethodArgumentTypeMismatchException
     * @param request the HTTP request
     * @return ErrorResponse with 400 status
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        
        String errorMessage = String.format("Invalid parameter '%s' with value '%s'. Expected type: %s",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        
        log.warn("Type mismatch at {}: {}", request.getRequestURI(), errorMessage);
        
        ErrorResponse error = new ErrorResponse(400, errorMessage, request.getRequestURI());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }


    /**
     * Handles custom AppException thrown from services.
     * Returns the HTTP status code specified in the exception.
     * 
     * <p><strong>Usage in service:</strong>
     * <pre>
     * throw new AppException("Vehicle not found with id: " + id, 404);
     * </pre>
     * 
     * @param ex the AppException instance
     * @param request the HTTP request (to extract the request URI)
     * @return standardized ErrorResponse with the specified status code
     */
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(
            AppException ex, 
            HttpServletRequest request) {

        log.warn("AppException at {}: {} (status {})", request.getRequestURI(), ex.getMessage(), ex.getStatusCode());
        
        ErrorResponse error = new ErrorResponse(
            ex.getStatusCode(),
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.valueOf(ex.getStatusCode()));
    }
    
    /**
     * Handles generic RuntimeException (e.g., from repositories).
     * Defaults to 404 (Not Found) status code.
     * 
     * <p>This is useful when orElseThrow() is used without a custom exception:
     * <pre>
     * repository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
     * </pre>
     * 
     * @param ex the RuntimeException instance
     * @param request the HTTP request
     * @return ErrorResponse with 404 status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request) {

        log.warn("RuntimeException at {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse error = new ErrorResponse(
            404,
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Fallback handler for any unhandled exception.
     * Returns 500 (Internal Server Error) to the client.
     * 
     * <p>This acts as a safety net, similar to the final catch-all in Express:
     * <pre>
     * app.use((err, req, res, next) => {
     *   res.status(500).json({ error: "Something went wrong!" });
     * });
     * </pre>
     * 
     * @param ex the Exception instance
     * @param request the HTTP request
     * @return ErrorResponse with 500 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request) {

        log.error("Unhandled exception at {}: ", request.getRequestURI(), ex);

        ErrorResponse error = new ErrorResponse(
            500,
            "Internal server error: " + ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
