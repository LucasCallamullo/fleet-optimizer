package com.auth.service;

import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import com.auth.exception.AppException;
import com.auth.config.KeycloakProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Service for interacting with Keycloak authentication server.
 * 
 * This service handles all Keycloak operations:
 * - User registration
 * - Login (password grant)
 * - Token refresh
 * - Logout
 * - User profile retrieval
 * - Admin operations (create user, assign roles)
 * 
 * All methods are reactive using WebClient and return Mono/Flux.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    private final WebClient keycloakWebClient;
    private final KeycloakProperties keycloakProperties;
    private final ObjectMapper objectMapper;

    // ================================================================
    // PUBLIC METHODS
    // ================================================================

    /**
     * Registers a new user in Keycloak.
     * 
     * Steps:
     * 1. Get admin token (required for user creation)
     * 2. Create user in Keycloak with provided data
     * 3. Assign role to the new user
     * 4. Login the user to get access/refresh tokens
     * 
     * @param request Registration data
     * @return AuthResponseDTO with tokens and user info
     */
    public Mono<AuthResponseDTO> registerUser(RegisterRequestDTO request) {
        log.info("Registering new user: {}", request.email());
        
        // Step 1: Obtain admin token for privileged operations
        return getAdminToken()
            // Step 2: Create user in Keycloak
            .flatMap(adminToken -> {
                log.debug("Admin token obtained, creating user...");
                return createKeycloakUser(request, adminToken)
                    // Step 3: Assign role to the user
                    .flatMap(userId -> {
                        log.debug("User created with ID: {}, assigning role...", userId);
                        return assignRole(userId, request.rol(), adminToken)
                            .thenReturn(userId);
                    });
            })
            // Step 4: Login the user to get tokens
            .flatMap(userId -> {
                log.info("User created with ID: {}, logging in...", userId);
                return login(request.email(), request.password());
            });
    }

    /**
     * Authenticates a user using Direct Access Grants (password flow).
     * 
     * Steps:
     * 1. Build form data with credentials
     * 2. POST to Keycloak token endpoint
     * 3. Parse response to extract tokens
     * 4. Decode JWT to get user info
     * 
     * @param email User's email (used as username)
     * @param password User's password
     * @return AuthResponseDTO with tokens and user info
     * @throws AppException if credentials are invalid (401)
     */
    public Mono<AuthResponseDTO> login(String email, String password) {
        log.info("Login attempt for user: {}", email);
        
        // Step 1: Build request body with credentials
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", keycloakProperties.getClientId());
        body.add("grant_type", "password");
        body.add("username", email);
        body.add("password", password);
        body.add("scope", "openid email profile");

        // Step 2: Make POST request to Keycloak
        return keycloakWebClient.post()
            .uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.getRealm())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            // Step 3: Handle errors
            .onStatus(status -> status.is4xxClientError(), response -> {
                log.error("Login failed with status: {}", response.statusCode());
                if (response.statusCode().value() == 401) {
                    throw new AppException("Invalid credentials", 401);
                }
                throw new AppException("Authentication service error", 500);
            })
            // Step 4: Parse response
            .bodyToMono(JsonNode.class)
            .map(response -> {
                log.debug("Login successful, parsing tokens...");
                // Extract tokens from response
                String accessToken = response.get("access_token").asText();
                String refreshToken = response.get("refresh_token").asText();
                int expiresIn = response.get("expires_in").asInt();

                // Step 5: Decode JWT to get user info
                UserInfoDTO userInfo = decodeToken(accessToken);

                // Step 6: Build and return response
                return new AuthResponseDTO(
                    accessToken,
                    refreshToken,
                    expiresIn,
                    userInfo
                );
            });
    }

    /**
     * Refreshes the access token using a valid refresh token.
     * 
     * Steps:
     * 1. Build form data with refresh token
     * 2. POST to Keycloak token endpoint with grant_type=refresh_token
     * 3. Parse response to get new access token
     * 4. Decode JWT to get updated user info
     * 
     * @param refreshToken Valid refresh token
     * @return AuthResponseDTO with new access token and user info
     * @throws AppException if refresh token is invalid (401)
     */
    public Mono<AuthResponseDTO> refreshToken(String refreshToken) {
        log.info("Refreshing access token...");
        
        // Step 1: Build request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", keycloakProperties.getClientId());
        body.add("grant_type", "refresh_token");
        body.add("refresh_token", refreshToken);

        // Step 2: Make POST request
        return keycloakWebClient.post()
            .uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.getRealm())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            // Step 3: Handle errors
            .onStatus(status -> status.is4xxClientError(), response -> {
                log.error("Refresh token failed with status: {}", response.statusCode());
                throw new AppException("Invalid refresh token", 401);
            })
            // Step 4: Parse response
            .bodyToMono(JsonNode.class)
            .map(response -> {
                log.debug("Refresh successful, parsing new tokens...");
                String accessToken = response.get("access_token").asText();
                int expiresIn = response.get("expires_in").asInt();

                // Step 5: Decode JWT for user info
                UserInfoDTO userInfo = decodeToken(accessToken);

                // Step 6: Return response (no new refresh token)
                return new AuthResponseDTO(
                    accessToken,
                    null,  // Keycloak doesn't return a new refresh token
                    expiresIn,
                    userInfo
                );
            });
    }

    /**
     * Logs out a user by invalidating the refresh token.
     * 
     * Steps:
     * 1. Build form data with refresh token
     * 2. POST to Keycloak logout endpoint
     * 3. Log success or failure
     * 
     * @param refreshToken Refresh token to invalidate
     * @return Mono<Void> indicating completion
     */
    public Mono<Void> logout(String refreshToken) {
        log.info("Logging out user...");
        
        // Step 1: Build request body
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", keycloakProperties.getClientId());
        body.add("refresh_token", refreshToken);

        // Step 2: Make POST request to logout endpoint
        return keycloakWebClient.post()
            .uri("/realms/{realm}/protocol/openid-connect/logout", keycloakProperties.getRealm())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            .bodyToMono(Void.class)
            // Step 3: Log success
            .doOnSuccess(v -> log.info("Logout successful"))
            // Step 4: Log but don't fail if logout fails
            .onErrorResume(e -> {
                log.warn("Logout error (ignoring): {}", e.getMessage());
                return Mono.empty();
            });
    }

    /**
     * Gets user information from the access token.
     * 
     * Steps:
     * 1. Extract token from Authorization header
     * 2. Decode JWT to get user info
     * 
     * @param authHeader Authorization header (Bearer token)
     * @return UserInfoDTO extracted from token
     */
    public Mono<UserInfoDTO> getUserInfo(String authHeader) {
        log.debug("Fetching user info from token");
        
        // Step 1: Extract token from header
        String token = authHeader.replace("Bearer ", "");
        
        // Step 2: Decode token and return user info
        return Mono.just(decodeToken(token));
    }

    // ================================================================
    // PRIVATE METHODS (Admin operations)
    // ================================================================

    /**
     * Obtains an admin token from Keycloak.
     * 
     * Steps:
     * 1. Build form data with admin credentials
     * 2. POST to Keycloak token endpoint
     * 3. Extract access_token from response
     * 
     * @return Mono<String> admin access token
     * @throws AppException if authentication fails
     */
    private Mono<String> getAdminToken() {
        log.debug("Obtaining admin token...");
        
        // Step 1: Build request body with admin credentials
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", "admin-cli");
        body.add("grant_type", "password");
        body.add("username", keycloakProperties.getAdminUser());
        body.add("password", keycloakProperties.getAdminPassword());

        // Step 2: Make POST request
        return keycloakWebClient.post()
            .uri("/realms/{realm}/protocol/openid-connect/token", keycloakProperties.getRealm())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(body))
            .retrieve()
            // Step 3: Handle errors
            .onStatus(status -> status.isError(), response -> {
                log.error("Failed to get admin token: {}", response.statusCode());
                throw new AppException("Failed to authenticate with Keycloak", 500);
            })
            // Step 4: Extract token from response
            .bodyToMono(JsonNode.class)
            .map(node -> {
                log.debug("Admin token obtained successfully");
                return node.get("access_token").asText();
            });
    }

    /**
     * Creates a new user in Keycloak.
     * 
     * Steps:
     * 1. Build user data map
     * 2. POST to Keycloak admin users endpoint
     * 3. Extract user ID from Location header
     * 
     * @param request User registration data
     * @param adminToken Admin access token
     * @return Mono<String> created user ID
     * @throws AppException if user already exists (409) or invalid data (400)
     */
    private Mono<String> createKeycloakUser(RegisterRequestDTO request, String adminToken) {
        log.debug("Creating Keycloak user: {}", request.email());
        
        // Step 1: Build user data payload
        Map<String, Object> userData = Map.of(
            "username", request.email(),
            "email", request.email(),
            "firstName", request.firstName() != null ? request.firstName() : "",
            "lastName", request.lastName() != null ? request.lastName() : "",
            "enabled", true,
            "emailVerified", false,
            "credentials", new Object[]{
                Map.of(
                    "type", "password",
                    "value", request.password(),
                    "temporary", false
                )
            }
        );

        // Step 2: Make POST request to create user
        return keycloakWebClient.post()
            .uri("/admin/realms/{realm}/users", keycloakProperties.getRealm())
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userData)
            .retrieve()
            // Step 3: Handle errors
            .onStatus(status -> status.is4xxClientError(), response -> {
                log.error("User creation failed with status: {}", response.statusCode());
                if (response.statusCode().value() == 409) {
                    throw new AppException("Email already registered", 409);
                }
                throw new AppException("Invalid user data", 400);
            })
            // Step 4: Extract user ID from Location header
            .toBodilessEntity()
            .map(response -> {
                String location = response.getHeaders().getFirst("Location");
                if (location != null) {
                    String[] parts = location.split("/");
                    String userId = parts[parts.length - 1];
                    log.debug("User created with ID: {}", userId);
                    return userId;
                }
                throw new AppException("Failed to create user", 500);
            });
    }

    /**
     * Assigns a role to a Keycloak user.
     * 
     * Steps:
     * 1. Get role details from Keycloak
     * 2. POST role mapping to user
     * 3. Log success or failure
     * 
     * @param userId Keycloak user ID
     * @param roleName Role name (admin, usuario, supervisor)
     * @param adminToken Admin access token
     * @return Mono<Void> indicating completion
     */
    private Mono<Void> assignRole(String userId, String roleName, String adminToken) {
        // Step 0: Default role if none provided
        if (roleName == null || roleName.isEmpty()) {
            roleName = "usuario";
            log.debug("No role provided, defaulting to 'usuario'");
        }

        String finalRoleName = roleName;
        log.debug("Assigning role '{}' to user {}", finalRoleName, userId);
        
        // Step 1: Get role details
        return keycloakWebClient.get()
            .uri("/admin/realms/{realm}/roles/{roleName}", keycloakProperties.getRealm(), finalRoleName)
            .header("Authorization", "Bearer " + adminToken)
            .retrieve()
            .bodyToMono(JsonNode.class)
            // Step 2: Assign role to user
            .flatMap(role -> {
                log.debug("Role details retrieved, assigning to user...");
                return keycloakWebClient.post()
                    .uri("/admin/realms/{realm}/users/{userId}/role-mappings/realm", 
                        keycloakProperties.getRealm(), userId)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(new Object[]{role})
                    .retrieve()
                    .toBodilessEntity()
                    .then();
            })
            // Step 3: Log success
            .doOnSuccess(v -> log.info("Role '{}' assigned to user {}", finalRoleName, userId))
            // Step 4: Log but don't fail if role assignment fails
            .onErrorResume(e -> {
                log.warn("Failed to assign role '{}': {}", finalRoleName, e.getMessage());
                return Mono.empty();
            });
    }

    // ================================================================
    // UTILITY METHODS
    // ================================================================

    private UserInfoDTO decodeToken(String token) {
        try {
            log.debug("Decoding JWT token...");
            
            // Step 1: Split JWT (header.payload.signature)
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                log.warn("Invalid token format: expected 3 parts, got {}", parts.length);
                throw new AppException("Invalid token format", 400);
            }
            
            // Step 2: Decode payload (base64)
            String payload = new String(Base64.getDecoder().decode(parts[1]));
            log.trace("Decoded payload: {}", payload);
            
            // Step 3: Parse JSON
            JsonNode json = objectMapper.readTree(payload);

            // ================================================================
            // Step 4: Extract user claims
            // ================================================================
            
            // Step 4.1: Basic claims (root level)
            String userId = json.path("sub").asText(null);
            String username = json.path("preferred_username").asText(null);
            String email = json.path("email").asText(null);
            String name = json.path("name").asText(null);
            String givenName = json.path("given_name").asText(null);
            String familyName = json.path("family_name").asText(null);
            boolean emailVerified = json.path("email_verified").asBoolean(false);
            
            // ================================================================
            // Step 4.2: Extract roles from "realm_access.roles"
            // ================================================================
            // Method 1: Using path() and findValuesAsText()
            // List<String> roles = json.path("realm_access").path("roles").findValuesAsText("roles");
            // This works but returns a List of values from "roles" fields.
            // 
            // Method 2: Extract directly from the array (more reliable)
            // ================================================================
            List<String> roles = List.of();
            JsonNode realmAccess = json.path("realm_access");
            if (!realmAccess.isMissingNode()) {
                JsonNode rolesNode = realmAccess.path("roles");
                if (rolesNode.isArray()) {
                    roles = new ArrayList<>();
                    for (JsonNode role : rolesNode) {
                        roles.add(role.asText());
                    }
                }
            }
            
            log.debug("Extracted roles: {}", roles);
            
            // ================================================================
            // Step 5: Build UserInfoDTO
            // ================================================================
            UserInfoDTO userInfo = new UserInfoDTO(
                userId,
                username,
                email,
                name,
                givenName,
                familyName,
                emailVerified,
                roles
            );
            
            log.debug("Token decoded successfully for user: {}", userInfo.email());
            return userInfo;
            
        } catch (Exception e) {
            log.error("Failed to decode token: {}", e.getMessage(), e);
            return UserInfoDTO.empty();
        }
    }
}