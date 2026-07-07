package com.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for refreshing an access token.
 * 
 * <p>Contains the refresh token used to obtain a new access token
 * without requiring the user to re-authenticate.
 * 
 * <p>Example usage:
 * <pre>
 * POST /api/v1/auth/refresh
 * {
 *     "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkazZ1OVVMSkppaFFJMDF5OE1uUEpGVUhsRlZuTDNXT0xEQlRZZDh5TUlJIn0..."
 * }
 * </pre>
 * 
 * @param refreshToken The refresh token obtained during login.
 *                     Used to request a new access token when the current one expires.
 *                     Must not be null or blank.
 */
public record RefreshTokenRequestDTO(
    
    /**
     * Refresh token string.
     * This token has a longer validity than the access token.
     * Must be valid and not expired.
     */
    @NotBlank(message = "Refresh token is required")
    String refreshToken
) {}