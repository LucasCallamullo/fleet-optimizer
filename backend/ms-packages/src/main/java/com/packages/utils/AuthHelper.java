// com.packages.utils.AuthHelper.java
package com.packages.utils;

import com.packages.exception.AppException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * AuthHelper - Utility for extracting user information from Gateway headers.
 * 
 * ================================================================
 * PURPOSE
 * ================================================================
 * 
 * This microservice (ms-packages) is an INTERNAL service that sits
 * behind the API Gateway. The Gateway is responsible for:
 * 
 * 1. Validating JWT tokens
 * 2. Extracting user claims (id, email, roles)
 * 3. Forwarding them as HTTP headers:
 *    - X-User-Id: User's unique identifier (sub from JWT)
 *    - X-User-Email: User's email address
 *    - X-User-Roles: Comma-separated list of roles
 *    - X-Auth-Token: Full JWT (optional, for debugging)
 * 
 * This helper reads those headers so we don't have to:
 * - Decode JWT again (the Gateway already did it)
 * - Add Spring Security dependencies to this service
 * - Duplicate code across controllers
 * 
 * ================================================================
 * HOW TO USE
 * ================================================================
 * 
 * Step 1: Inject the helper in your controller
 * private final AuthHelper authHelper;
 * 
 * Step 2: Call methods to get user information
 * String userId = authHelper.getCurrentUserId();
 * boolean isAdmin = authHelper.isAdmin();
 * List<String> roles = authHelper.getCurrentUserRoles();
 * 
 * Step 3: All methods throw AppException if user is not authenticated
 * The GlobalExceptionHandler will catch and format the error response.
 * 
 * ================================================================
 * SECURITY NOTE
 * ================================================================
 * 
 * This service trusts the Gateway to have properly authenticated
 * the user. DO NOT expose this service directly to the internet.
 * It should ONLY be accessible through the Gateway.
 * 
 * ================================================================
 * HEADERS FROM GATEWAY
 * ================================================================
 * 
 * Header           | Source     | Description
 * -----------------|------------|-----------------------------------
 * X-User-Id        | JWT sub    | User's unique identifier (UUID)
 * X-User-Email     | JWT email  | User's email address
 * X-User-Roles     | JWT roles  | Comma-separated roles (admin,user)
 * X-Auth-Token     | JWT raw    | Full JWT (for debugging)
 * 
 * These headers are added by AuthenticationHeaderFilter in the Gateway.
 * See: com.fleetoptimizer.gateway.filter.AuthenticationHeaderFilter
 */
@Slf4j
@Component
public class AuthHelper {

    // ================================================================
    // 1. HEADER CONSTANTS
    // ================================================================
    // These must match the header names set by the Gateway's
    // AuthenticationHeaderFilter.
    // ================================================================
    
    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_AUTH_TOKEN = "X-Auth-Token";

    // ================================================================
    // 2. GET CURRENT USER ID
    // ================================================================
    
    /**
     * Gets the current user ID from the X-User-Id header.
     * This header is added by the Gateway after JWT validation.
     * 
     * Step by step:
     * 1. Retrieve the header value from the HttpServletRequest
     * 2. Validate that the value is not null or empty
     * 3. If valid, return the user ID as a String
     * 4. If invalid, throw AppException with status 401
     * 
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return String - The user ID (never null)
     * @throws AppException - If user ID is missing (status 401)
     */
    public String getCurrentUserId(HttpServletRequest request) {
        String userId = getHeaderValue(request, HEADER_USER_ID);
        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id header is missing - request rejected");
            throw new AppException("Authentication required. Please log in again.", 401);
        }
        return userId;
    }

    /**
     * Gets the current user ID from the current request context.
     * This method uses RequestContextHolder to get the current request.
     * 
     * Step by step:
     * 1. Get the current HttpServletRequest from the context
     * 2. Extract the X-User-Id header value
     * 3. Validate and return the user ID
     * 
     * @return String - The user ID (never null)
     * @throws AppException - If user ID is missing (status 401)
     */
    public String getCurrentUserId() {
        return getCurrentUserId(getCurrentRequest());
    }

    // ================================================================
    // 3. GET CURRENT USER EMAIL
    // ================================================================

    /**
     * Gets the current user email from the X-User-Email header.
     * 
     * Step by step:
     * 1. Retrieve the header value from the HttpServletRequest
     * 2. Validate that the value is not null or empty
     * 3. If valid, return the email as a String
     * 4. If invalid, throw AppException with status 401
     * 
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return String - The user email (never null)
     * @throws AppException - If email is missing (status 401)
     */
    public String getCurrentUserEmail(HttpServletRequest request) {
        String email = getHeaderValue(request, HEADER_USER_EMAIL);
        if (email == null || email.isEmpty()) {
            log.warn("X-User-Email header is missing");
            throw new AppException("User email not found. Please log in again.", 401);
        }
        return email;
    }

    /**
     * Gets the current user email from the current request context.
     * 
     * @return String - The user email (never null)
     * @throws AppException - If email is missing (status 401)
     */
    public String getCurrentUserEmail() {
        return getCurrentUserEmail(getCurrentRequest());
    }

    // ================================================================
    // 4. GET USER ROLES
    // ================================================================

    /**
     * Gets the current user roles from the X-User-Roles header.
     * Roles are comma-separated: "admin,user,editor"
     * 
     * Step by step:
     * 1. Retrieve the header value from the HttpServletRequest
     * 2. If the header is null or empty, return an empty list
     * 3. Split the comma-separated string into a List of Strings
     * 
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return List<String> - List of roles (empty if none found)
     */
    public List<String> getCurrentUserRoles(HttpServletRequest request) {
        String rolesHeader = getHeaderValue(request, HEADER_USER_ROLES);
        if (rolesHeader == null || rolesHeader.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(rolesHeader.split(","));
    }

    /**
     * Gets the current user roles from the current request context.
     * 
     * @return List<String> - List of roles (empty if none found)
     */
    public List<String> getCurrentUserRoles() {
        return getCurrentUserRoles(getCurrentRequest());
    }

    // ================================================================
    // 5. CHECK IF USER IS ADMIN
    // ================================================================

    /**
     * Checks if the current user has admin role.
     * 
     * Step by step:
     * 1. Verify that the user is authenticated (getCurrentUserId)
     * 2. Get the list of roles from the header
     * 3. Check if any role matches "admin", "ROLE_admin", or "administrador"
     * 4. Return true if admin, false otherwise
     * 
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return boolean - true if user has admin role, false otherwise
     * @throws AppException - If user is not authenticated (status 401)
     */
    public boolean isAdmin(HttpServletRequest request) {
        // Step 1: Verify authentication first
        getCurrentUserId(request);
        
        // Step 2: Get roles and check for admin
        List<String> roles = getCurrentUserRoles(request);
        return roles.stream().anyMatch(role -> 
            role.equalsIgnoreCase("admin") || 
            role.equalsIgnoreCase("ROLE_admin") ||
            role.equalsIgnoreCase("administrador")
        );
    }

    /**
     * Checks if the current user has admin role from the current request context.
     * 
     * @return boolean - true if user has admin role, false otherwise
     * @throws AppException - If user is not authenticated (status 401)
     */
    public boolean isAdmin() {
        return isAdmin(getCurrentRequest());
    }

    // ================================================================
    // 6. CHECK SPECIFIC ROLE
    // ================================================================

    /**
     * Checks if the current user has a specific role (case-insensitive).
     * 
     * Step by step:
     * 1. Verify that the user is authenticated (getCurrentUserId)
     * 2. Get the list of roles from the header
     * 3. Check if any role matches the provided role (case-insensitive)
     * 4. Return true if the user has the role, false otherwise
     * 
     * @param role - The role to check (e.g., "editor", "viewer")
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return boolean - true if user has the role, false otherwise
     * @throws AppException - If user is not authenticated (status 401)
     * 
     * Example:
     * if (authHelper.hasRole("editor", request)) {
     *     // Allow editing
     * }
     */
    public boolean hasRole(String role, HttpServletRequest request) {
        // Step 1: Verify authentication first
        getCurrentUserId(request);
        
        // Step 2: Get roles and check for the specific role
        List<String> roles = getCurrentUserRoles(request);
        return roles.stream().anyMatch(r -> r.equalsIgnoreCase(role));
    }

    /**
     * Checks if the current user has a specific role from the current request context.
     * 
     * @param role - The role to check (e.g., "editor", "viewer")
     * @return boolean - true if user has the role, false otherwise
     * @throws AppException - If user is not authenticated (status 401)
     */
    public boolean hasRole(String role) {
        return hasRole(role, getCurrentRequest());
    }

    // ================================================================
    // 7. GET FULL JWT TOKEN (for debugging)
    // ================================================================

    /**
     * Gets the full JWT token from the X-Auth-Token header.
     * This is useful for debugging or for services that need to forward it.
     * 
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return String - Full JWT token, or null if not found
     */
    public String getAuthToken(HttpServletRequest request) {
        return getHeaderValue(request, HEADER_AUTH_TOKEN);
    }

    /**
     * Gets the full JWT token from the current request context.
     * 
     * @return String - Full JWT token, or null if not found
     */
    public String getAuthToken() {
        return getHeaderValue(getCurrentRequest(), HEADER_AUTH_TOKEN);
    }

    // ================================================================
    // 8. GET ALL USER INFORMATION (combined)
    // ================================================================

    /**
     * Gets all user information as a UserInfo record object.
     * 
     * Step by step:
     * 1. Get the user ID (validates authentication)
     * 2. Get the user email
     * 3. Get the user roles
     * 4. Check if the user is admin
     * 5. Get the auth token (optional)
     * 6. Build and return the UserInfo object
     * 
     * @param request - The HttpServletRequest containing headers from Gateway
     * @return UserInfo - Object containing all user information
     * @throws AppException - If user is not authenticated (status 401)
     */
    public UserInfo getUserInfo(HttpServletRequest request) {
        String userId = getCurrentUserId(request);
        return UserInfo.builder()
            .userId(userId)
            .email(getCurrentUserEmail(request))
            .roles(getCurrentUserRoles(request))
            .isAdmin(isAdmin(request))
            .authToken(getAuthToken(request))
            .build();
    }

    /**
     * Gets all user information from the current request context.
     * 
     * @return UserInfo - Object containing all user information
     * @throws AppException - If user is not authenticated (status 401)
     */
    public UserInfo getUserInfo() {
        return getUserInfo(getCurrentRequest());
    }

    // ================================================================
    // 9. PRIVATE HELPER METHODS
    // ================================================================

    /**
     * Gets the current HttpServletRequest from the request context.
     * 
     * Step by step:
     * 1. Get the request attributes from RequestContextHolder
     * 2. Cast the attributes to ServletRequestAttributes
     * 3. If available, return the HttpServletRequest
     * 4. If not available, throw AppException with status 500
     * 
     * @return HttpServletRequest - The current request
     * @throws AppException - If request context is not available (status 500)
     */
    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                return attributes.getRequest();
            }
        } catch (Exception exception) {
            log.debug("Could not get current request: {}", exception.getMessage());
        }
        throw new AppException("Could not retrieve request context. Please try again.", 500);
    }

    /**
     * Gets a header value from the request.
     * 
     * Step by step:
     * 1. Validate that the request is not null
     * 2. If request is null, throw AppException with status 500
     * 3. If request is valid, retrieve the header value by name
     * 4. Return the header value (may be null if header not present)
     * 
     * @param request - The HttpServletRequest
     * @param headerName - The name of the header to retrieve
     * @return String - The header value, or null if not present
     * @throws AppException - If request is null (status 500)
     */
    private String getHeaderValue(HttpServletRequest request, String headerName) {
        if (request == null) {
            throw new AppException("Request context not available. Please try again.", 500);
        }
        return request.getHeader(headerName);
    }

    // ================================================================
    // 10. INNER CLASS - UserInfo Record
    // ================================================================

    /**
     * UserInfo - Data Transfer Object for all user information.
     * 
     * This record holds all user data extracted from the Gateway headers.
     * 
     * @param userId - User's unique identifier (UUID from JWT "sub" claim)
     * @param email - User's email address (from JWT "email" claim)
     * @param roles - List of user roles (from JWT "realm_access.roles")
     * @param isAdmin - True if user has admin role
     * @param authToken - Full JWT token (for debugging or forwarding)
     */
    public record UserInfo(
        String userId,
        String email,
        List<String> roles,
        boolean isAdmin,
        String authToken
    ) {
        /**
         * Creates a builder for UserInfo.
         * 
         * @return Builder - A new builder instance
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * Builder class for constructing UserInfo objects.
         */
        public static class Builder {
            private String userId;
            private String email;
            private List<String> roles;
            private boolean isAdmin;
            private String authToken;

            /**
             * Sets the user ID.
             * 
             * @param userId - The user's unique identifier
             * @return Builder - This builder instance for chaining
             */
            public Builder userId(String userId) {
                this.userId = userId;
                return this;
            }

            /**
             * Sets the user email.
             * 
             * @param email - The user's email address
             * @return Builder - This builder instance for chaining
             */
            public Builder email(String email) {
                this.email = email;
                return this;
            }

            /**
             * Sets the user roles.
             * 
             * @param roles - List of user roles
             * @return Builder - This builder instance for chaining
             */
            public Builder roles(List<String> roles) {
                this.roles = roles;
                return this;
            }

            /**
             * Sets whether the user is an admin.
             * 
             * @param isAdmin - True if user has admin role
             * @return Builder - This builder instance for chaining
             */
            public Builder isAdmin(boolean isAdmin) {
                this.isAdmin = isAdmin;
                return this;
            }

            /**
             * Sets the full JWT token.
             * 
             * @param authToken - The full JWT token
             * @return Builder - This builder instance for chaining
             */
            public Builder authToken(String authToken) {
                this.authToken = authToken;
                return this;
            }

            /**
             * Builds the UserInfo object.
             * 
             * @return UserInfo - The constructed UserInfo object
             */
            public UserInfo build() {
                return new UserInfo(userId, email, roles, isAdmin, authToken);
            }
        }
    }
}