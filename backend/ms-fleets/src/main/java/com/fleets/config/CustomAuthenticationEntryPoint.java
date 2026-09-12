package com.fleets.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom Authentication Entry Point for handling unauthenticated requests.
 *
 * Intercepts authentication failures and returns a consistent 401 Unauthorized
 * response instead of the default Spring Security behavior.
 *
 * Why this is needed:
 * - By default, Spring Security returns 403 Forbidden when @PreAuthorize fails,
 *   even when the user is not authenticated at all.
 * - REST best practices require 401 for unauthenticated requests.
 * - 403 should be reserved for authenticated users who lack permissions.
 *
 * Architecture context:
 * - In a microservices setup, authentication is handled by the Gateway.
 * - The Gateway validates the JWT and adds X-User-Id and X-User-Roles headers.
 * - This service should only receive authenticated requests from the Gateway.
 * - Direct requests without headers must be rejected with 401.
 *
 * Response format:
 *
 *   {
 *     "status": 401,
 *     "success": false,
 *     "detail": "Unauthorized",
 *     "path": "/api/v1/vehicles"
 *   }
 *
 * SecurityConfig usage:
 *
 *   http.exceptionHandling(exceptions -> exceptions
 *       .authenticationEntryPoint(customAuthenticationEntryPoint));
 *
 * @see org.springframework.security.web.AuthenticationEntryPoint
 * @see com.fleets.config.SecurityConfig
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles authentication failures and returns a structured 401 response.
     *
     * Triggered when:
     * - The request has no X-User-Id header (GatewayHeaderAuthenticationFilter skips it).
     * - The request has invalid authentication (though the Gateway should catch this).
     * - The request bypasses the Gateway and hits this service directly.
     *
     * The response body is JSON and mirrors the GlobalExceptionHandler format,
     * so clients can parse errors the same way regardless of the source.
     *
     * @param request       the HTTP request that triggered the authentication failure
     * @param response      the HTTP response to write the error to
     * @param authException the exception that caused the authentication failure
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        // Set HTTP status to 401 Unauthorized.
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Set Content-Type so the client interprets the body as JSON.
        response.setContentType("application/json");

        // Write a structured JSON error response consistent with GlobalExceptionHandler.
        response.getWriter().write("""
            {
                "status": 401,
                "success": false,
                "detail": "Unauthorized",
                "path": "%s"
            }
            """.formatted(request.getRequestURI())
        );
    }
}