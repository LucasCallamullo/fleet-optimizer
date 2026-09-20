package com.auth.controller;

import com.auth.dto.request.LoginRequestDTO;
import com.auth.dto.request.RefreshTokenRequestDTO;
import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import com.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
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
public class AuthController {

    private final AuthService authService;

    // Explicit injection via constructor and @Qualifier
    public AuthController(@Qualifier("auth0Service") AuthService authService) {
        this.authService = authService;
    }

    // ================================================================
    // + PUBLIC ENDPOINTS (no authentication required)
    // ================================================================

    /**
     * Registers a new user with AuthService interface (use OAuth impl).
     * 
     * This endpoint is PUBLIC - anyone can register.
     * No authentication required.
     * 
     * @param request Registration request with user data
     * @return AuthResponseDTO with tokens and user info
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        log.info("Registering new user: {}", request.email());
        return authService.registerUser(request);
    }

    /**
     * Authenticates a user and returns access/refresh tokens.
     * 
     * This endpoint is PUBLIC - anyone can login.
     * No authentication required.
     * 
     * @param request Login credentials
     * @return AuthResponseDTO with tokens and user info
     */
    @PostMapping("/login")
    public Mono<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        log.info("User login: {}", request.email());
        return authService.login(request);
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * 
     * This endpoint is PUBLIC - no authentication required.
     * The refresh token itself acts as the authentication mechanism.
     * 
     * @param request Refresh token
     * @return AuthResponseDTO with new access token
     */
    @PostMapping("/refresh")
    @PreAuthorize("isAuthenticated()")
    public Mono<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        log.info("Refreshing token");
        return authService.refreshToken(request);
    }

    // ================================================================
    // + PROTECTED ENDPOINTS (require authentication)
    // ================================================================

    /**
     * Logs out a user by invalidating the refresh token.
     * 
     * This endpoint requires authentication.
     * The Gateway validates the JWT before forwarding the request.
     * 
     * @param request Refresh token to invalidate (optional)
     * @return Mono<Void> indicating completion
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> logout(@RequestBody(required = false) RefreshTokenRequestDTO request) {
        log.info("Logging out user");

        // Step 1: If refresh token provided, invalidate it
        if (request != null && request.refreshToken() != null) {
            return authService.logout(request);
        }

        // Step 2: If no token, just return empty (client-side logout only)
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
    @PreAuthorize("isAuthenticated()")
    public Mono<UserInfoDTO> getProfile(@RequestHeader("Authorization") String authHeader) {
        log.info("Fetching user profile");
        return authService.getUserInfo(authHeader);
    }
}