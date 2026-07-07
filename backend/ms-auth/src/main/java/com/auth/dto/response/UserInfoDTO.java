package com.auth.dto.response;

import java.util.List;

/**
 * DTO representing user information extracted from the JWT token.
 * 
 * <p>Contains user details and roles parsed from the token payload.
 * 
 * <p>Fields are extracted from the JWT claims:
 * <pre>
 * {
 *   "sub": "12345",           → id
 *   "preferred_username": "johndoe", → username
 *   "email": "john@example.com", → email
 *   "name": "John Doe",       → name
 *   "given_name": "John",     → givenName
 *   "family_name": "Doe",     → familyName
 *   "email_verified": true,   → emailVerified
 *   "realm_access": {
 *     "roles": ["usuario", "admin"] → roles
 *   }
 * }
 * </pre>
 * 
 * @param id User's unique identifier (sub claim)
 * @param username User's username (preferred_username claim)
 * @param email User's email address
 * @param name User's full name
 * @param givenName User's first name (given_name claim)
 * @param familyName User's last name (family_name claim)
 * @param emailVerified Whether the email has been verified
 * @param roles List of user roles for authorization
 */
public record UserInfoDTO(
    
    /**
     * User's unique identifier in Keycloak.
     * Corresponds to the "sub" claim in the JWT.
     */
    String id,
    
    /**
     * User's username.
     * Corresponds to the "preferred_username" claim.
     */
    String username,
    
    /**
     * User's email address.
     * Corresponds to the "email" claim.
     */
    String email,
    
    /**
     * User's full name.
     * Corresponds to the "name" claim.
     */
    String name,
    
    /**
     * User's first name.
     * Corresponds to the "given_name" claim.
     */
    String givenName,
    
    /**
     * User's last name.
     * Corresponds to the "family_name" claim.
     */
    String familyName,
    
    /**
     * Whether the user's email has been verified in Keycloak.
     * Corresponds to the "email_verified" claim.
     */
    boolean emailVerified,
    
    /**
     * List of roles assigned to the user.
     * Extracted from "realm_access.roles".
     * Used for authorization with @PreAuthorize annotations.
     */
    List<String> roles
) {
    
    /**
     * Creates an empty UserInfoDTO with all fields null/empty.
     * Used as a fallback when token decoding fails.
     * 
     * @return Empty UserInfoDTO instance
     */
    public static UserInfoDTO empty() {
        return new UserInfoDTO(
            null, null, null, 
            null, null, null, 
            false, List.of()
        );
    }
}