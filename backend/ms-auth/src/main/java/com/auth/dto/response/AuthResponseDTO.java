package com.auth.dto.response;

/**
 * Response DTO for authentication operations (login, refresh, register).
 * 
 * <p>Contains the tokens and user information returned after successful authentication.
 * 
 * <p>Example response:
 * <pre>
 * {
 *     "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkazZ1OVVMSkppaFFJMDF5OE1uUEpGVUhsRlZuTDNXT0xEQlRZZDh5TUlJIn0...",
 *     "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJkazZ1OVVMSkppaFFJMDF5OE1uUEpGVUhsRlZuTDNXT0xEQlRZZDh5TUlJIn0...",
 *     "expiresIn": 300,
 *     "user": {
 *         "id": "12345",
 *         "username": "johndoe",
 *         "email": "john@example.com",
 *         "roles": ["usuario"]
 *     }
 * }
 * </pre>
 * 
 * @param accessToken JWT access token for API authorization
 * @param refreshToken Token used to obtain a new access token
 * @param expiresIn Access token validity in seconds
 * @param user Authenticated user information
 */
public record AuthResponseDTO(
    
    /**
     * JWT access token.
     * Include this in the Authorization header for API requests:
     * Authorization: Bearer {accessToken}
     */
    String accessToken,
    
    /**
     * Refresh token.
     * Use this to obtain a new access token when the current one expires.
     * Has a longer validity period than the access token.
     */
    String refreshToken,
    
    /**
     * Time in seconds until the access token expires.
     * Default is typically 300 seconds (5 minutes).
     */
    int expiresIn,
    
    /**
     * User profile information extracted from the access token.
     */
    UserInfoDTO user
) {}