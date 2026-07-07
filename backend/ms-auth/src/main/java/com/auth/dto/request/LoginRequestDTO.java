package com.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for user login.
 * 
 * <p>Contains the credentials required to authenticate a user.
 * Both fields are required and cannot be blank.
 * 
 * <p>Example usage:
 * <pre>
 * POST /api/v1/auth/login
 * {
 *     "email": "user@example.com",
 *     "password": "securePassword123"
 * }
 * </pre>
 * 
 * @param email User's email address (used as username in Keycloak)
 * @param password User's password (validated against Keycloak)
 */
public record LoginRequestDTO(
    
    /**
     * User's email address.
     * Used as the username in Keycloak.
     * Must not be null or empty.
     */
    @NotBlank(message = "Email is required")
    String email,
    
    /**
     * User's password.
     * Must not be null or empty.
     * Validated against Keycloak during authentication.
     */
    @NotBlank(message = "Password is required")
    String password
) {}