package com.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;

import reactor.core.publisher.Mono;
import org.springframework.security.authentication.ReactiveAuthenticationManager;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Security Configuration for ms-auth.
 * 
 * This configuration handles authentication using headers forwarded from the Gateway.
 * 
 * Architecture Flow:
 * 1. Gateway validates JWT and adds X-User-Id and X-User-Roles headers
 * 2. This service reads those headers and creates an Authentication object
 * 3. The Authentication is used by @PreAuthorize annotations
 * 4. Any exceptions propagate to GlobalExceptionHandler
 * 
 * Key Difference from Standard Approach:
 * - Uses AuthenticationWebFilter instead of a custom WebFilter
 * - This is the NATIVE Spring Security way to add custom authentication
 * - Ensures proper integration with Spring Security's filter chain
 * - Exceptions are properly propagated to GlobalExceptionHandler
 * 
 * This approach was chosen because:
 * 1. It integrates properly with Spring Security's reactive filter chain
 * 2. AuthenticationWebFilter is the standard way to add custom auth in WebFlux
 * 3. @PreAuthorize can read the Authentication from the Security Context
 * 4. Errors are handled by the global exception handler automatically
 */
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity // For WebFlux, use EnableReactiveMethodSecurity (not EnableMethodSecurity)
public class SecurityConfig {

    /**
     * Main security filter chain configuration.
     * 
     * Step-by-step:
     * 1. Disable CSRF (stateless API with JWT from Gateway)
     * 2. Define public endpoints (no authentication needed)
     * 3. All other endpoints require authentication
     * 4. Add custom AuthenticationWebFilter at AUTHENTICATION order
     * 5. Configure exception handling to propagate errors to GlobalExceptionHandler
     * 
     * @param http ServerHttpSecurity instance
     * @return SecurityWebFilterChain configured for the application
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // Step 1: Disable CSRF - we use JWT tokens from Gateway, no session
            .csrf(csrf -> csrf.disable())
            
            // Step 2 & 3: Define authorization rules
            .authorizeExchange(exchanges -> exchanges
                // Public endpoints - no authentication required
                .pathMatchers("/api/v1/auth/register", "/api/v1/auth/login", "/api/v1/auth/refresh").permitAll()
                .pathMatchers("/api/v1/auth/test/public", "/api/v1/auth/test/info").permitAll()
                // All other endpoints require authentication
                .anyExchange().authenticated()
            )
            
            // Step 4: Register custom authentication filter
            // This filter reads headers from Gateway and creates Authentication
            // It executes at AUTHENTICATION order (before AUTHORIZATION where @PreAuthorize runs)
            .addFilterAt(gatewayAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            
            // Step 5: Configure exception handling to propagate to GlobalExceptionHandler
            // This ensures that AccessDeniedException and AuthenticationException
            // are caught and handled by the @ControllerAdvice
            .exceptionHandling(exceptionHandling -> exceptionHandling
                // Access denied (403) - user authenticated but lacks required role
                .accessDeniedHandler((exchange, deniedException) -> Mono.error(deniedException))
                // Authentication failed (401) - user not authenticated
                .authenticationEntryPoint((exchange, authException) -> Mono.error(authException))
            )
            .build();
    }

    /**
     * Creates the custom AuthenticationWebFilter.
     * 
     * This filter extracts user information from Gateway headers
     * and creates a Spring Security Authentication object.
     * 
     * Why AuthenticationWebFilter instead of custom WebFilter?
     * - It's the native Spring Security way to add custom authentication
     * - It properly integrates with the security filter chain
     * - It ensures that @PreAuthorize can read the Authentication
     * - It handles error propagation correctly
     * 
     * @return Configured AuthenticationWebFilter
     */
    private AuthenticationWebFilter gatewayAuthenticationFilter() {
        // Step 1: Create a ReactiveAuthenticationManager
        // This is a simple manager that accepts any authentication
        // because we already trust the Gateway's validation
        // The authentication comes pre-validated from the Gateway
        ReactiveAuthenticationManager authenticationManager = Mono::just;
        
        // Step 2: Create the AuthenticationWebFilter with the manager
        AuthenticationWebFilter filter = new AuthenticationWebFilter(authenticationManager);
        
        // Step 3: Set the custom converter that reads headers
        filter.setServerAuthenticationConverter(gatewayAuthenticationConverter());
        
        return filter;
    }

    /**
     * Converts Gateway headers into a Spring Security Authentication object.
     * 
     * This converter reads the headers added by the Gateway:
     * - X-User-Id: User's unique identifier
     * - X-User-Roles: Comma-separated list of user roles
     * 
     * If no X-User-Id header is found, it returns Mono.empty() which means
     * the user is anonymous (will result in 401 if endpoint is protected).
     * 
     * @return ServerAuthenticationConverter that processes headers
     */
    private ServerAuthenticationConverter gatewayAuthenticationConverter() {
        return exchange -> {
            // Step 1: Extract headers from Gateway
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            String rolesHeader = exchange.getRequest().getHeaders().getFirst("X-User-Roles");

            // Step 2: If no userId header, return empty (anonymous)
            // This will result in 401 if the endpoint is protected with @PreAuthorize
            if (userId == null || userId.isEmpty()) {
                return Mono.empty();
            }

            // Step 3: Parse roles from header
            // Roles come as comma-separated string: "admin,user"
            // Convert to uppercase and add ROLE_ prefix for Spring Security
            // Replace hyphens with underscores for valid authority names
            List<SimpleGrantedAuthority> authorities;
            if (rolesHeader != null && !rolesHeader.isEmpty()) {
                authorities = Arrays.stream(rolesHeader.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase().replace("-", "_")))
                    .collect(Collectors.toList());
            } else {
                authorities = List.of();
            }

            // Step 4: Create and return the Authentication object
            // This will be used by @PreAuthorize annotations
            return Mono.just(new UsernamePasswordAuthenticationToken(userId, null, authorities));
        };
    }
}