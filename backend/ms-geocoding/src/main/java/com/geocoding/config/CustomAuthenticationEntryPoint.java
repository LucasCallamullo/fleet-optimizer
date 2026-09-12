package com.geocoding.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Custom Authentication Entry Point for handling unauthenticated requests in WebFlux.
 *
 * Intercepts authentication failures and returns a consistent 401 Unauthorized
 * response instead of the default Spring Security WebFlux behavior.
 *
 * Triggered when:
 * - The request has no valid authentication (no X-User-Id header, invalid token, etc.).
 * - The request bypasses the Gateway and hits this service directly.
 *
 * The response body is JSON and mirrors the GlobalExceptionHandler format,
 * so clients can parse errors the same way regardless of the source.
 *
 * @see org.springframework.security.web.server.ServerAuthenticationEntryPoint
 * @see com.geocoding.config.SecurityConfig
 */
@Component
public class CustomAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Handles authentication failures and writes a structured 401 response.
     *
     * @param exchange the current server exchange
     * @param authException the exception that caused the authentication failure
     * @return a Mono that completes when the response has been written
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException authException) {
        ServerHttpResponse response = exchange.getResponse();

        // Set HTTP status to 401 Unauthorized.
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // Set Content-Type so the client interprets the body as JSON.
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Build the same error shape used by GlobalExceptionHandler.
        Map<String, Object> body = Map.of(
                "status", 401,
                "success", false,
                "detail", "Unauthorized",
                "path", exchange.getRequest().getURI().getPath()
        );

        // Serialize and write the body reactively.
        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            bytes = "{\"status\":401,\"success\":false,\"detail\":\"Unauthorized\"}"
                    .getBytes(StandardCharsets.UTF_8);
        }

        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }
}