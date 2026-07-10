package com.packages.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Imperative Filter for Servlet-based Microservices (ms-fleets, ms-routes).
 * 
 * This filter reads the authentication headers injected by the API Gateway
 * and populates the Spring Security Context for the current request.
 * 
 * Architecture Flow:
 * ===================
 * 1. Gateway validates JWT and adds headers (X-User-Id, X-User-Roles)
 * 2. Gateway forwards request to this microservice
 * 3. This filter extracts the headers
 * 4. Creates a Spring Security Authentication object
 * 5. Stores it in the SecurityContextHolder (ThreadLocal)
 * 6. @PreAuthorize annotations can now read the Authentication
 * 7. Request proceeds to the controller
 * 
 * This filter runs ONCE per request (extends OncePerRequestFilter)
 * and is designed for Servlet-based (Tomcat) microservices.
 * 
 * For Reactive (WebFlux) microservices, a different filter is required.
 * 
 * Trust Model:
 * ============
 * This filter TRUSTS the Gateway headers without validation.
 * The Gateway is responsible for:
 * - JWT signature validation
 * - Token expiration checking
 * - User authentication
 * 
 * This service is only responsible for:
 * - Reading headers
 * - Creating Authentication object
 * - Enabling @PreAuthorize annotations
 * 
 * This is acceptable because:
 * - Gateway and microservices are in the same internal network
 * - Gateway is the single entry point to the system
 * - Headers cannot be forged from outside the internal network
 */
public class GatewayHeaderAuthenticationFilter extends OncePerRequestFilter {

    /**
     * Main filter method that processes each request.
     * 
     * Step-by-step:
     * 1. Extract X-User-Id and X-User-Roles headers from the request
     * 2. If X-User-Id is missing, continue without authentication (anonymous)
     * 3. Parse and normalize roles (convert to uppercase, replace hyphens with underscores)
     * 4. Create Spring Security Authentication object
     * 5. Store Authentication in SecurityContextHolder
     * 6. Continue the filter chain
     * 
     * @param request The HTTP request containing Gateway headers
     * @param response The HTTP response
     * @param filterChain The filter chain to continue
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Step 1: Extract headers from Gateway
        // These headers are added by the Gateway after JWT validation
        // X-User-Id: User's unique identifier (sub claim from JWT)
        // X-User-Roles: Comma-separated list of roles (from realm_access.roles)
        String userId = request.getHeader("X-User-Id");
        String rolesHeader = request.getHeader("X-User-Roles");

        // Step 2: If no user ID header, continue anonymously
        // This will result in 401 if the endpoint is protected with @PreAuthorize
        if (userId == null || userId.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Parse and normalize roles
        // Roles come as comma-separated string: "admin,user"
        // We convert to uppercase and add ROLE_ prefix for Spring Security
        // Hyphens are replaced with underscores for valid authority names
        // Examples:
        //   "admin,user" → ["ROLE_ADMIN", "ROLE_USER"]
        //   "default-roles-dds-materia" → ["ROLE_DEFAULT_ROLES_DDS_MATERIA"]
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

        // Step 4: Create the Spring Security Authentication object
        // UsernamePasswordAuthenticationToken is the standard implementation
        // Principal: userId (String)
        // Credentials: null (not needed, already validated by Gateway)
        // Authorities: List of SimpleGrantedAuthority (roles)
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // Step 5: Store Authentication in SecurityContextHolder
        // SecurityContextHolder uses ThreadLocal to store the authentication
        // This makes it available to @PreAuthorize annotations and anywhere
        // else in the request thread that needs to access the current user
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Step 6: Continue the filter chain
        // The request now has Authentication in the Security Context
        // @PreAuthorize annotations will validate roles
        // Controllers can access the user via @AuthenticationPrincipal
        filterChain.doFilter(request, response);
    }
}