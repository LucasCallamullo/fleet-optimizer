package com.geocoding.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Custom Access Denied Handler for handling authenticated requests that lack
 * the required permissions.
 *
 * In WebFlux, security exceptions are raised outside the DispatcherHandler
 * pipeline, so the GlobalExceptionHandler (@ControllerAdvice) never sees them.
 * This handler writes a structured 403 response directly, mirroring the
 * ErrorResponse format used by GlobalExceptionHandler.
 *
 * Triggered when:
 * - The user is authenticated but does not have the required role.
 * - A @PreAuthorize expression evaluates to false.
 *
 * Response format:
 *
 *   {
 *     "status": 403,
 *     "success": false,
 *     "detail": "Forbidden",
 *     "path": "/api/v1/distance"
 *   }
 *
 * @see org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
 * @see com.geocoding.config.SecurityConfig
 */
@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public CustomAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handles access denied failures and writes a structured 403 response.
     * 
     * @param exchange the current server exchange
     * @param denied   the exception that caused the access denial
     * @return a Mono that completes when the response has been written
     */
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        ServerHttpResponse response = exchange.getResponse();

        // Set HTTP status to 403 Forbidden.
        response.setStatusCode(HttpStatus.FORBIDDEN);

        // Set Content-Type so the client interprets the body as JSON.
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Build a body consistent with the GlobalExceptionHandler format.
        Map<String, Object> body = Map.of(
                "status", 403,
                "success", false,
                "detail", "Forbidden",
                "path", exchange.getRequest().getURI().getPath()
        );

        // Serialize the body. Fall back to a minimal JSON if serialization fails.
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = "{\"status\":403,\"success\":false,\"detail\":\"Forbidden\"}"
                    .getBytes(StandardCharsets.UTF_8);
        }

        // Write the buffer reactively.
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}