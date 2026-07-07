package com.auth.controller;

import com.auth.dto.response.UserInfoDTO;
import com.auth.service.KeycloakService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Test endpoints for authentication demonstration.
 * 
 * These endpoints are used to test and demonstrate different authentication levels:
 * - Public endpoints: No authentication required
 * - Protected endpoints: Require valid JWT token
 * - Role-based endpoints: Require specific roles (USER or ADMIN)
 * 
 * Note: Authentication is handled by the Gateway. This service receives
 * the user context via headers added by the Gateway.
 * 
 * Roles:
 * - USER: Basic authenticated user
 * - ADMIN: Administrator with elevated privileges
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth/test")
@RequiredArgsConstructor
public class TestAuthController {

    private final KeycloakService keycloakService;

    // ================================================================
    // PUBLIC ENDPOINTS (no authentication required)
    // ================================================================

    /**
     * Public endpoint - accessible by anyone.
     * No authentication required.
     * 
     * @return Public greeting message
     */
    @GetMapping("/public")
    public Mono<Map<String, Object>> publicEndpoint() {
        log.info("Public endpoint accessed");
        
        return Mono.just(Map.of(
            "success", true,
            "message", "Hello public! This endpoint is accessible to everyone without authentication",
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Public info endpoint - shows available test endpoints.
     * No authentication required.
     * 
     * @return Information about available test endpoints
     */
    @GetMapping("/info")
    public Mono<Map<String, Object>> infoEndpoint() {
        log.info("Info endpoint accessed");
        
        return Mono.just(Map.of(
            "success", true,
            "message", "Authentication Test System with Keycloak",
            "endpoints", Map.of(
                "public", new String[]{
                    "GET /api/v1/auth/test/public",
                    "GET /api/v1/auth/test/info"
                },
                "protected", new String[]{
                    "GET /api/v1/auth/test/authenticated - Any authenticated user",
                    "GET /api/v1/auth/test/user - Requires USER or ADMIN role",
                    "GET /api/v1/auth/test/admin - Requires ADMIN role",
                    "GET /api/v1/auth/test/multi-role - Requires USER or ADMIN",
                    "GET /api/v1/auth/test/profile - User profile"
                }
            ),
            "auth_required", "Bearer token in Authorization header",
            "base_url", "/api/v1/auth/test"
        ));
    }

    // ================================================================
    // PROTECTED ENDPOINTS (require authentication)
    // ================================================================

    /**
     * Protected endpoint - any authenticated user can access.
     * Requires valid JWT token (any role).
     * 
     * @param userId User ID from Gateway header
     * @param username Username from Gateway header
     * @param email Email from Gateway header
     * @return Authenticated user greeting
     */
    @GetMapping("/authenticated")
    @PreAuthorize("isAuthenticated()")
    public Mono<Map<String, Object>> authenticatedEndpoint(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String email) {
        
        log.info("Authenticated endpoint accessed by user: {}", userId);
        
        String displayName = email != null ? email : "Authenticated User";
        
        return Mono.just(Map.of(
            "success", true,
            "message", String.format("Hello authenticated %s! You have access because you are logged in", displayName),
            "user", Map.of(
                "id", userId,
                "email", email
            )
        ));
    }

    /**
     * User/Admin endpoint - requires USER or ADMIN role.
     * Uses @PreAuthorize with hasAnyRole.
     * 
     * @param userId User ID from Gateway header
     * @param username Username from Gateway header
     * @param roles User roles from Gateway header
     * @return User role greeting
     */
    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Mono<Map<String, Object>> userEndpoint(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String username,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        
        log.info("User endpoint accessed by user: {} with roles: {}", userId, roles);
        
        String displayName = username != null ? username : (userId != null ? userId : "User");
        String roleList = roles != null ? roles : "none";
        
        return Mono.just(Map.of(
            "success", true,
            "message", String.format("Hello user %s! This endpoint is for users (and admins too)", displayName),
            "user", Map.of(
                "id", userId,
                "username", username,
                "roles", roleList
            )
        ));
    }

    /**
     * Admin endpoint - requires ADMIN role only.
     * Uses @PreAuthorize with hasRole.
     * 
     * @param userId User ID from Gateway header
     * @param username Username from Gateway header
     * @param roles User roles from Gateway header
     * @return Admin greeting with admin actions
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Map<String, Object>> adminEndpoint(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String username,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        
        log.info("Admin endpoint accessed by user: {} with roles: {}", userId, roles);
        
        String displayName = username != null ? username : (userId != null ? userId : "Admin");
        
        return Mono.just(Map.of(
            "success", true,
            "message", String.format("Hello Administrator %s! Welcome to the admin panel", displayName),
            "user", Map.of(
                "id", userId,
                "username", username,
                "roles", roles
            ),
            "adminOnly", true,
            "actions", new String[]{
                "create_user",
                "delete_user",
                "view_reports",
                "manage_vehicles",
                "assign_routes"
            }
        ));
    }

    /**
     * Multi-role endpoint - requires USER or ADMIN role.
     * Shows which roles can access this endpoint.
     * 
     * @param userId User ID from Gateway header
     * @param username Username from Gateway header
     * @param roles User roles from Gateway header
     * @return Multi-role greeting with allowed roles
     */
    @GetMapping("/multi-role")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public Mono<Map<String, Object>> multiRoleEndpoint(
            @RequestHeader(value = "X-User-Id", required = false) String userId,
            @RequestHeader(value = "X-User-Email", required = false) String username,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        
        log.info("Multi-role endpoint accessed by user: {} with roles: {}", userId, roles);
        
        String displayName = username != null ? username : (userId != null ? userId : "User");
        String roleList = roles != null ? roles : "none";
        
        return Mono.just(Map.of(
            "success", true,
            "message", String.format("Hello %s! This endpoint is accessible to users and administrators", displayName),
            "user", Map.of(
                "id", userId,
                "username", username,
                "roles", roleList
            ),
            "allowedRoles", new String[]{"USER", "ADMIN"}
        ));
    }

    /**
     * Profile endpoint - returns full user profile.
     * Uses the token from the Authorization header.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return User profile information
     */
    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public Mono<UserInfoDTO> profileEndpoint(@RequestHeader("Authorization") String authHeader) {
        log.info("Profile endpoint accessed");
        return keycloakService.getUserInfo(authHeader);
    }
}