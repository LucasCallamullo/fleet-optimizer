package com.fleetoptimizer.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
// import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * Authentication Header Filter.
 * 
 * This filter extracts user information from the already-validated JWT
 * and forwards it to downstream microservices via HTTP headers.
 * 
 * RESPONSIBILITY:
 * - Reads the authenticated JWT from Spring Security context
 * - Extracts user claims (id, email, roles)
 * - Adds them as headers to the request
 * - Forwards to the target microservice
 * 
 * NOTE: This filter does NOT validate the JWT.
 * Validation is handled by Spring Security's oauth2ResourceServer.
 * This filter only EXTRACTS and FORWARDS data.
 * 
 * EXECUTION ORDER: HIGHEST_PRECEDENCE (runs before routing)
 */
@Component
public class AuthenticationHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        
        // ================================================================
        // STEP 1: Get the Security Context (reactively)
        // ================================================================
        // ReactiveSecurityContextHolder provides access to the authenticated user.
        // This is populated by Spring Security after JWT validation.
        // ================================================================
        return ReactiveSecurityContextHolder.getContext()
        
            // ================================================================
            // STEP 2: Extract the Authentication object
            // ================================================================
            // Authentication contains the user's principal and authorities.
            // After JWT validation, the principal is a Jwt object.
            // ================================================================
            .map(ctx -> ctx.getAuthentication())
            
            // ================================================================
            // STEP 3: Check if the principal is a Jwt
            // ================================================================
            // If authenticated, the principal is a Jwt object.
            // If not authenticated (anonymous), it's something else.
            // ================================================================
            .map(authentication -> {
                if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                    // ============================================================
                    // STEP 4: Cast to Jwt and extract claims
                    // ============================================================
                    // Jwt contains all claims from the token:
                    // - sub: user ID (unique identifier)
                    // - email: user's email
                    // - roles: user's roles/authorities
                    // - exp: expiration time
                    // - iss: issuer (Keycloak)
                    // ============================================================
                    Jwt jwt = (Jwt) authentication.getPrincipal();
                    
                    // ============================================================
                    // STEP 5: Add user data as HTTP headers
                    // ============================================================
                    // These headers are forwarded to the downstream microservice:
                    // - X-User-Id: user's unique identifier
                    // - X-User-Email: user's email
                    // - X-User-Roles: comma-separated roles
                    // - X-Auth-Token: the full JWT (for microservice validation)
                    // ============================================================
                    return addHeaders(exchange, jwt);
                }
                // If not authenticated, return the exchange unchanged
                return exchange;
            })
            
            // ================================================================
            // STEP 6: Handle case where authentication is missing
            // ================================================================
            // If the Security Context is empty (no authentication),
            // return the original exchange without modifications.
            // ================================================================
            .defaultIfEmpty(exchange)
            
            // ================================================================
            // STEP 7: Continue the filter chain
            // ================================================================
            // After adding headers (or not), proceed to the next filter
            // and eventually to the route (target microservice).
            // ================================================================
            .flatMap(chain::filter);
    }

    /**
     * Adds user information as HTTP headers to the request.
     * 
     * @param exchange The current ServerWebExchange
     * @param jwt The validated Jwt object
     * @return Modified ServerWebExchange with added headers
     */
    @SuppressWarnings("unchecked")
    private ServerWebExchange addHeaders(ServerWebExchange exchange, Jwt jwt) {
        // Step 1: Extract claims from Jwt
        String userId = jwt.getClaimAsString("sub");
        String email = jwt.getClaimAsString("email");
        
        // Step 2: Extract roles from "realm_access.roles"
        // The roles are nested inside realm_access, not at the root level
        List<String> roles = null;
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            roles = (List<String>) realmAccess.get("roles");
        }
        
        // Step 3: Handle null values safely
        String userIdHeader = userId != null ? userId : "";
        String emailHeader = email != null ? email : "";
        String rolesHeader = roles != null ? String.join(",", roles) : "";
        
        // Step 4: Build and return the modified exchange
        // Headers added:
        // - X-User-Id: For the microservice to know which user is making the request
        // - X-User-Email: For business logic (e.g., send notifications)
        // - X-User-Roles: For authorization checks in microservices
        // - X-Auth-Token: Full JWT for microservices that need to validate it
        return exchange.mutate()
            .request(r -> r
                .header("X-User-Id", userIdHeader)
                .header("X-User-Email", emailHeader)
                .header("X-User-Roles", rolesHeader)
                .header("X-Auth-Token", jwt.getTokenValue())
            )
            .build();
    }

    /**
     * Returns the execution order for this filter.
     * 
     * Ordered.HIGHEST_PRECEDENCE (-2147483648) ensures this filter runs
     * before other filters and before the request is routed.
     * 
     * @return Order value
     */
    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}