package com.routes.config;

import com.routes.exception.AppException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Feign clients.
 * Provides custom error handling for all Feign clients.
 * 
 * This class intercepts HTTP errors from external microservices
 * and converts them into AppException with appropriate status codes.
 */
@Configuration
@Slf4j
public class FeignConfig {

    /**
     * Creates a custom ErrorDecoder that wraps Feign exceptions into AppException.
     * This is applied globally to all Feign clients using this configuration.
     * 
     * @return ErrorDecoder instance
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default() {
            @Override
            public Exception decode(String methodKey, Response response) {
                // ============================================================
                // STEP 1: Extract the HTTP status code from the response
                // ============================================================
                // Example: 404, 500, 503, etc.
                int status = response.status();

                // ============================================================
                // STEP 2: Extract the error message from the response body
                // ============================================================
                // The response body typically contains a JSON error structure:
                // {
                //   "status": 404,
                //   "error": "Vehicle not found",
                //   "path": "/api/v1/vehicles"
                // }
                // 
                // We try to read the body as a String to preserve the original error message.
                // If we cannot read it, we use a generic message.
                String message = "Error calling external service";  // Default fallback message

                try {
                    if (response.body() != null) {
                        // Read the response body as a String
                        // This is the raw JSON/plain text returned by the external service
                        message = new String(response.body().asInputStream().readAllBytes());
                    }
                } catch (Exception e) {
                    // If we cannot read the body (e.g., stream closed, empty body),
                    // we keep the default message and log a warning
                    log.warn("Could not read error response body for {}: {}", methodKey, e.getMessage());
                }

                // ============================================================
                // STEP 3: Log the error details for debugging
                // ============================================================
                // This helps track down issues in external service calls
                log.error("Feign error - method: {}, status: {}, message: {}", methodKey, status, message);

                // ============================================================
                // STEP 4: Wrap the error in AppException and propagate it
                // ============================================================
                // This converts any FeignException (e.g., FeignException.NotFound,
                // FeignException.ServiceUnavailable) into a business-friendly
                // AppException that our GlobalExceptionHandler can handle.
                return new AppException(message, status);
            }
        };
    }
}