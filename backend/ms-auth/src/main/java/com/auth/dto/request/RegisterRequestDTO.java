package com.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for user registration.
 * 
 * <p>Contains all data required to create a new user in Keycloak.
 * All fields except firstName, lastName, and rol are required.
 * 
 * <p>Example usage:
 * <pre>
 * POST /api/v1/auth/register
 * {
 *     "email": "newuser@example.com",
 *     "password": "securePassword123",
 *     "firstName": "John",
 *     "lastName": "Doe",
 *     "rol": "usuario"
 * }
 * </pre>
 * 
 * @param email User's email address (also used as username)
 * @param password User's password (minimum 6 characters)
 * @param firstName User's first name (optional)
 * @param lastName User's last name (optional)
 * @param rol User's role (optional, defaults to "usuario")
 */
public record RegisterRequestDTO(
    
    /**
     * User's email address.
     * Must be a valid email format.
     * Used as username in Keycloak.
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    String email,
    
    /**
     * User's password.
     * Minimum length of 6 characters.
     * Stored securely in Keycloak.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    String password,
    
    /**
     * User's first name.
     * Optional field - can be null or empty.
     */
    String firstName,
    
    /**
     * User's last name.
     * Optional field - can be null or empty.
     */
    String lastName,
    
    /**
     * User's role for authorization.
     * Optional - defaults to "usuario" if not provided.
     * Valid values: "admin", "usuario", "supervisor"
     */
    String rol
) {}