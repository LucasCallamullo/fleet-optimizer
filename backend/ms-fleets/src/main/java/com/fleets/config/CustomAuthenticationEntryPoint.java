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
 * This component intercepts authentication failures and returns a consistent
 * 401 Unauthorized response instead of the default Spring Security behavior.
 * 
 * Why is this needed?
 * - By default, Spring Security returns 403 Forbidden when @PreAuthorize fails
 * - This includes cases where the user is not authenticated at all
 * - REST API best practices require 401 for unauthenticated requests
 * - 403 should be reserved for authenticated users who lack permissions
 * 
 * Architecture Context:
 * - In a microservices architecture, authentication is typically handled by the Gateway
 * - The Gateway validates JWT and adds X-User-Id and X-User-Roles headers
 * - This service should receive authenticated requests from the Gateway
 * - However, direct requests without headers should be rejected with 401
 * - This entry point ensures consistent error responses even if the Gateway is bypassed
 * 
 * Response Format:
 * <pre>
 * {
 *   "status": 401,
 *   "error": "Unauthorized",
 *   "path": "/api/v1/vehicles"
 * }
 * </pre>
 * 
 * Integration with SecurityConfig:
 *   @Bean
 *   public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
 *       return http
 *           .exceptionHandling(exceptions -> exceptions
 *               .authenticationEntryPoint(customAuthenticationEntryPoint)
 *           )
 *           .build();
 *   }
 * 
 * @see org.springframework.security.web.AuthenticationEntryPoint
 * @see com.fleets.config.SecurityConfig
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * Handles authentication failures and returns a structured 401 response.
     * 
     * <p><strong>Execution Flow:</strong>
     * <ol>
     *   <li>Called when Spring Security detects an AuthenticationException</li>
     *   <li>This occurs when the request has no valid authentication context</li>
     *   <li>Sets HTTP status to 401 (Unauthorized)</li>
     *   <li>Sets Content-Type to application/json</li>
     *   <li>Writes a structured JSON error response</li>
     * </ol>
     * 
     * <p><strong>When is this triggered?</strong>
     * <ul>
     *   <li>Request without X-User-Id header (GatewayHeaderAuthenticationFilter skips)</li>
     *   <li>Request with invalid authentication (though Gateway should catch this)</li>
     *   <li>Direct requests to this service bypassing the Gateway</li>
     * </ul>
     * 
     * <p><strong>Why JSON format?</strong>
     * <ul>
     *   <li>Consistent with the GlobalExceptionHandler error format</li>
     *   <li>Includes the request path for debugging</li>
     *   <li>Allows clients to parse errors programmatically</li>
     * </ul>
     * 
     * @param request The HTTP request that triggered the authentication failure
     * @param response The HTTP response to write the error to
     * @param authException The exception that caused the authentication failure
     * @throws IOException if an I/O error occurs while writing the response
     */
    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        
        // ================================================================
        // STEP 1: Set HTTP status to 401 Unauthorized
        // ================================================================
        // This indicates that the request lacks valid authentication credentials
        // The client should authenticate (e.g., obtain a valid JWT token)
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // ================================================================
        // STEP 2: Set Content-Type header
        // ================================================================
        // Ensures the client interprets the response as JSON
        response.setContentType("application/json");
        
        // ================================================================
        // STEP 3: Write JSON error response
        // ================================================================
        // Structured format consistent with GlobalExceptionHandler
        // Includes:
        //   - status: HTTP status code (401)
        //   - error: Human-readable error message
        //   - path: Request URI that caused the error
        // ================================================================
        response.getWriter().write("""
            {
                "status": 401,
                "error": "Unauthorized",
                "path": "%s"
            }
            """.formatted(request.getRequestURI())
        );
    }
}