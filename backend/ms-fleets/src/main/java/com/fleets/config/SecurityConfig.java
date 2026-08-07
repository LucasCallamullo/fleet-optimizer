package com.fleets.config;

import com.fleets.filter.GatewayHeaderAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Traditional Spring MVC Microservices (ms-fleets, ms-routes).
 * 
 * This configuration is designed for microservices that run on Tomcat (Servlet-based)
 * and receive authentication context from the API Gateway via headers.
 * 
 * Architecture Flow:
 * ===================
 * 1. Client sends request with JWT to Gateway
 * 2. Gateway validates JWT and adds X-User-Id, X-User-Roles headers
 * 3. Gateway forwards request to this service
 * 4. GatewayHeaderAuthenticationFilter reads headers and creates Authentication
 * 5. @PreAuthorize annotations use this Authentication for role validation
 * 6. Request reaches the controller if authorized
 * 
 * Key Differences from ms-auth (Reactive):
 * =========================================
 * - This uses @EnableWebSecurity (Servlet-based), not @EnableWebFluxSecurity
 * - SecurityFilterChain instead of SecurityWebFilterChain
 * - UsernamePasswordAuthenticationFilter instead of AuthenticationWebFilter
 * - SessionCreationPolicy.STATELESS for JWT-based authentication
 * 
 * This service trusts the Gateway and does NOT validate JWT itself.
 * The Gateway is responsible for:
 * - JWT signature validation
 * - Token expiration checking
 * - Adding user context headers
 * 
 * This service is only responsible for:
 * - Reading headers from Gateway
 * - Creating Authentication object
 * - Role-based authorization (@PreAuthorize)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enables @PreAuthorize for method-level security in imperative controllers
public class SecurityConfig {

    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    public SecurityConfig(CustomAuthenticationEntryPoint authenticationEntryPoint) {
        this.authenticationEntryPoint = authenticationEntryPoint;
    }

    /**
     * Configures the security filter chain for this microservice.
     * 
     * Step-by-step:
     * 1. Disable CSRF (stateless API, no session needed)
     * 2. Set session management to STATELESS (no server-side sessions)
     * 3. Define public endpoints (Swagger/OpenAPI documentation)
     * 4. All other endpoints require authentication
     * 5. Add custom header filter before Spring Security's default authentication filter
     * 
     * @param http HttpSecurity instance for configuration
     * @return SecurityFilterChain configured for the application
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Step 1: Disable CSRF protection
            // CSRF is not needed for stateless APIs with JWT authentication
            // The Gateway already validates the JWT, so CSRF attacks are not applicable
            .csrf(csrf -> csrf.disable())
            
            // Step 2: Configure session management
            // STATELESS means no session is created or used
            // Each request must contain the JWT token (validated by Gateway)
            // This is essential for horizontal scaling and microservices
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Step 3: Define authorization rules for endpoints
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - accessible without authentication
                // OpenAPI/Swagger documentation for API exploration
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // All other endpoints require authentication
                // The Authentication must be present in the Security Context
                // This is provided by GatewayHeaderAuthenticationFilter
                .anyRequest().authenticated()
            )

            // step 3.5: handler 401 errors 
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(authenticationEntryPoint)  
            )
            
            // Step 4: Register custom header authentication filter
            // This filter reads the X-User-Id and X-User-Roles headers
            // Adds them to the Security Context as Authentication
            // Executes BEFORE UsernamePasswordAuthenticationFilter (which would do nothing)
            // 
            // Filter Order:
            // 1. GatewayHeaderAuthenticationFilter (reads headers, sets Authentication)
            // 2. UsernamePasswordAuthenticationFilter (bypasses, since we're stateless)
            // 3. @PreAuthorize validation (checks roles)
            // 4. Controller execution
            .addFilterBefore(new GatewayHeaderAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .build();
    }
}