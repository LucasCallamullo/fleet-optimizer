package com.auth.controller;

import com.auth.dto.request.LoginRequestDTO;
import com.auth.dto.request.RefreshTokenRequestDTO;
import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import com.auth.service.KeycloakService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST controller for authentication operations.
 * 
 * Handles:
 * - User registration
 * - Login
 * - Token refresh
 * - Logout
 * - User profile retrieval
 * 
 * Security note:
 * - Login, register, and refresh are PUBLIC endpoints (no token required)
 * - Logout and profile require authentication (token must be valid)
 * - Authentication is handled by the Gateway, not by this service
 * - This service receives the validated token from the Gateway via headers
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KeycloakService keycloakService;

    // ================================================================
    // PUBLIC ENDPOINTS (no authentication required)
    // ================================================================

    /**
     * Registers a new user in Keycloak.
     * 
     * Steps:
     * 1. Receive registration data from request body
     * 2. Validate input (@Valid)
     * 3. Delegate to KeycloakService for user creation
     * 4. Return AuthResponseDTO with tokens after successful registration
     * 
     * This endpoint is PUBLIC - anyone can register.
     * No authentication required.
     * 
     * @param request Registration request with user data
     * @return AuthResponseDTO with tokens and user info
     */
    @PostMapping("/register")
    public Mono<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        // Step 1: Log the registration attempt
        log.info("Registering new user: {}", request.email());
        
        // Step 2: Delegate to service
        return keycloakService.registerUser(request);
    }

    /**
     * Authenticates a user and returns access/refresh tokens.
     * 
     * Steps:
     * 1. Receive login credentials from request body
     * 2. Validate input (@Valid)
     * 3. Delegate to KeycloakService for authentication
     * 4. Return AuthResponseDTO with tokens and user info
     * 
     * This endpoint is PUBLIC - anyone can login.
     * No authentication required.
     * 
     * @param request Login credentials
     * @return AuthResponseDTO with tokens and user info
     */
    @PostMapping("/login")
    public Mono<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        // Step 1: Log the login attempt
        log.info("User login: {}", request.email());
        
        // Step 2: Delegate to service
        return keycloakService.login(request.email(), request.password());
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * 
     * Steps:
     * 1. Receive refresh token from request body
     * 2. Validate input (@Valid)
     * 3. Delegate to KeycloakService for token refresh
     * 4. Return new AuthResponseDTO with fresh access token
     * 
     * This endpoint is PUBLIC - no authentication required.
     * The refresh token itself acts as the authentication mechanism.
     * 
     * @param request Refresh token
     * @return AuthResponseDTO with new access token
     */
    @PostMapping("/refresh")
    public Mono<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        // Step 1: Log the refresh attempt
        log.info("Refreshing token");
        
        // Step 2: Delegate to service
        return keycloakService.refreshToken(request.refreshToken());
    }

    // ================================================================
    // PROTECTED ENDPOINTS (require authentication)
    // ================================================================

    /**
     * Logs out a user by invalidating the refresh token.
     * 
     * Steps:
     * 1. Receive refresh token from request body (optional)
     * 2. If token provided, delegate to KeycloakService for logout
     * 3. Return empty response on success
     * 
     * This endpoint requires authentication.
     * The Gateway validates the JWT before forwarding the request.
     * 
     * @param request Refresh token to invalidate (optional)
     * @return Mono<Void> indicating completion
     */
    @PostMapping("/logout")
    public Mono<Void> logout(@RequestBody(required = false) RefreshTokenRequestDTO request) {
        // Step 1: Log the logout attempt
        log.info("Logging out user");
        
        // Step 2: If refresh token provided, invalidate it
        if (request != null && request.refreshToken() != null) {
            return keycloakService.logout(request.refreshToken());
        }
        
        // Step 3: If no token, just return empty (client-side logout only)
        return Mono.empty();
    }

    /**
     * Retrieves the authenticated user's profile from the token.
     * 
     * Steps:
     * 1. Extract Authorization header (Bearer token)
     * 2. Delegate to KeycloakService to decode token
     * 3. Return UserInfoDTO with user profile data
     * 
     * This endpoint requires authentication.
     * The Gateway validates the JWT before forwarding the request.
     * 
     * @param authHeader Authorization header with Bearer token
     * @return UserInfoDTO with user profile information
     */
    @GetMapping("/profile")
    public Mono<UserInfoDTO> getProfile(@RequestHeader("Authorization") String authHeader) {
        // Step 1: Log the profile request
        log.info("Fetching user profile");
        
        // Step 2: Delegate to service
        return keycloakService.getUserInfo(authHeader);
    }
}