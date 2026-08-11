package com.routes.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Configuration class for Feign clients to propagate authentication and user context.
 * 
 * This interceptor automatically adds the JWT token and Gateway headers from the
 * current request to all outgoing Feign client requests. This allows microservices
 * to propagate user authentication context when calling other services.
 * 
 * How it works:
 * 1. When a request is made to ms-routes, the user's JWT and Gateway headers
 *    (X-User-Id, X-User-Roles) are present in the request
 * 2. ms-routes makes a Feign call to another microservice (e.g., ms-packages)
 * 3. This interceptor reads the original request's headers
 * 4. Copies them to the outgoing Feign request
 * 5. The target microservice receives the headers and can authenticate the user
 * 
 * This enables:
 * - End-to-end authentication across microservices
 * - User context propagation (who is making the request)
 * - Role-based authorization in downstream services
 * - Audit trail across service boundaries
 * - Consistent security model across all microservices
 * 
 * Security note:
 * - Headers are only propagated if present in the original request
 * - If no token is present, no Authorization header is added
 * - This preserves security (no fake tokens or headers are created)
 * - The Gateway is the single source of truth for authentication
 * 
 * Usage:
 * <pre>
 * &#64;FeignClient(
 *     name = "ms-packages",
 *     url = "${app.clients.packages.url}",
 *     configuration = {FeignConfig.class, FeignClientConfig.class}
 * )
 * public interface PackageClient {
 *     // ...
 * }
 * </pre>
 * 
 * @see org.springframework.web.context.request.RequestContextHolder
 * @see feign.RequestInterceptor
 */
@Configuration
public class FeignClientConfig {

    /**
     * Creates a RequestInterceptor that propagates authentication and user context
     * from the current request to outgoing Feign requests.
     * 
     * Step-by-step:
     * 1. Get the current HTTP request from RequestContextHolder
     * 2. Extract the Authorization header (Bearer token) if present
     * 3. Extract Gateway headers (X-User-Id, X-User-Roles, X-User-Email) if present
     * 4. Add all extracted headers to the outgoing Feign request
     * 5. Also adds a custom header X-Propagated to indicate headers were propagated
     * 
     * This ensures that the target microservice receives the same authentication
     * context as if the request came directly from the Gateway.
     * 
     * @return RequestInterceptor instance
     */
    @Bean
    public RequestInterceptor requestTokenInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // Step 1: Get the current request context
                // RequestContextHolder holds the current request in a ThreadLocal
                ServletRequestAttributes attributes = 
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                
                // Step 2: If we have a request, extract the headers
                if (attributes != null) {
                    // Step 3: Extract and propagate Authorization header (JWT token)
                    String token = attributes.getRequest().getHeader("Authorization");
                    if (token != null && !token.isEmpty()) {
                        template.header("Authorization", token);
                    }
                    
                    // Step 4: Extract and propagate Gateway user context headers
                    // These headers are added by the Gateway after JWT validation
                    // X-User-Id: User's unique identifier from Keycloak (sub claim)
                    String userId = attributes.getRequest().getHeader("X-User-Id");
                    if (userId != null && !userId.isEmpty()) {
                        template.header("X-User-Id", userId);
                    }
                    
                    // X-User-Roles: Comma-separated list of roles from Keycloak
                    String userRoles = attributes.getRequest().getHeader("X-User-Roles");
                    if (userRoles != null && !userRoles.isEmpty()) {
                        template.header("X-User-Roles", userRoles);
                    }
                    
                    // X-User-Email: User's email from Keycloak
                    String userEmail = attributes.getRequest().getHeader("X-User-Email");
                    if (userEmail != null && !userEmail.isEmpty()) {
                        template.header("X-User-Email", userEmail);
                    }
                    
                    // Step 5: Add custom header to indicate the request was propagated
                    // This helps downstream services identify that the request came
                    // from an authenticated source via the Gateway
                    template.header("X-Propagated", "true");
                }
                // If no headers exist, the Feign request is made without authentication
                // The target service will handle it as an unauthenticated request
                // (which should result in 401 if the endpoint is protected)
            }
        };
    }
}