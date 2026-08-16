package com.fleetoptimizer.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Security configuration for the API Gateway.
 * 
 * This class configures:
 * 1. CORS - Allows cross-origin requests from frontend
 * 2. JWT Validation - Decodes and validates JWT tokens from Keycloak
 * 3. Authorization - Defines public and protected routes
 * 
 * Spring Cloud Gateway uses WebFlux (reactive), so we use reactive security.
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    // ================================================================
    // INJECT PROPERTIES FROM application.yml
    // ================================================================

    @Value("${app.keycloak.jwks-uri}")
    private String keycloakJwksUri;

    /**
     * Main security filter chain.
     * Configures CORS, CSRF, public routes, and JWT validation.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) throws Exception {
        return http
            // Step 1: CORS configuration - ACTIVE
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Step 2: Disable CSRF (stateless API)
            .csrf(csrf -> csrf.disable())
            
            // Step 3: Authorization rules
            .authorizeExchange(exchanges -> exchanges
                // PERMITIR OPTIONS PREFLIGHT SIN AUTENTICACION
                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public endpoints (no authentication required)
                .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                .pathMatchers("/api/v1/auth/**").permitAll()
                // All other endpoints require authentication
                .anyExchange().authenticated()
            )
            
            // Step 4: JWT validation
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder()))
            )
            .build();
    }

    /**
     * JWT Decoder using Keycloak's public key.
     * 
     * The JWK (JSON Web Key) set is fetched from Keycloak's endpoint.
     * The keys are cached by NimbusJwtDecoder automatically.
     * 
     * Caching behavior:
     * - First request: fetches keys from Keycloak (~1ms overhead)
     * - Subsequent requests: uses cached keys (no network call)
     * - Refresh: keys are refreshed every 5 minutes (default)
     * - If keys haven't changed, no new network calls are made
     * 
     * This means there is NO latency penalty per request after the initial fetch.
     */
    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return NimbusReactiveJwtDecoder.withJwkSetUri(keycloakJwksUri).build();
    }

    /**
     * CORS configuration.
     * Allows cross-origin requests from frontend applications.
     * 
     * IMPORTANT: When using withCredentials: true,
     * allowedOrigins CANNOT be "*".
     * Must specify exact origins.
     * 
     * In production, replace with specific origins:
     * - React: http://localhost:3000, http://localhost:5173
     * - Production: https://myapp.com
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // Specific origins (NOT wildcard)
        config.setAllowedOrigins(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:5173"
        ));
        
        // Allow credentials (JWT in headers)
        config.setAllowCredentials(true);
        
        // Allowed methods (including OPTIONS for preflight)
        config.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        
        // Allowed headers
        config.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "X-User-Id",
            "X-User-Email",
            "X-User-Roles",
            "X-Auth-Token",
            "Cookie"
        ));
        
        // Exposed headers (frontend can read these)
        config.setExposedHeaders(Arrays.asList(
            "X-User-Id",
            "X-User-Email",
            "X-User-Roles",
            "Authorization"
        ));
        
        // Max age for preflight requests (1 hour)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}