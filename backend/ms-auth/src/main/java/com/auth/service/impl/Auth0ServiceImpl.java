package com.auth.service.impl;

import com.auth.dto.request.LoginRequestDTO;
import com.auth.dto.request.RefreshTokenRequestDTO;
import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import com.auth.exception.AppException;
import com.auth.service.AuthService;
import com.auth.config.Auth0Properties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

/**
 * Service implementation for Auth0 Identity Provider.
 * Replaces Keycloak while preserving the reactive contract defined in AuthService.
 */
@Slf4j
@Service("auth0Service")
@RequiredArgsConstructor
public class Auth0ServiceImpl implements AuthService {

    private final WebClient auth0WebClient;
    private final Auth0Properties auth0Properties;
    private final ObjectMapper objectMapper;

    // ! this claim Customs MUST be EQUALS to OAuth ClaimKey on JWT
    public static String claimCustomRoles = "https://fo.dev";

    // ================================================================
    // -- PUBLIC METHODS
    // ================================================================

    @Override
    public Mono<AuthResponseDTO> registerUser(RegisterRequestDTO request) {
        log.info("Registering new user in Auth0: {}", request.email());

        // Step 1: Obtain M2M token for Management API operations
        return getManagementApiToken()
            // Step 2: Create user in Auth0
            .flatMap(managementToken -> {
                log.debug("Management token obtained, creating user...");
                return createAuth0User(request, managementToken);
            })
            // Step 3: Login the user to return fresh tokens to the client
            .flatMap(userId -> {
                log.info("User created in Auth0 with ID: {}, logging in...", userId);
                var loginRequest = new LoginRequestDTO(request.email(), request.password());
                return login(loginRequest);
            });
    }

    /**
     * Authenticates a end-user against Auth0 using the Resource Owner Password Realm Grant flow.
     *
     * ! Unlike registration, this method does not require an administrative M2M token.
     * It sends the user's raw credentials directly to Auth0's {@code /oauth/token} endpoint
     * for verification within the default database connection ({@code Username-Password-Authentication}).
     *
     *
     * @param request the login payload containing user email and password
     * @return a {@link Mono} emitting the {@link AuthResponseDTO} populated with JWT tokens and user profile info
     * @throws AppException if credentials are invalid (401) or the authentication service encounters an error (500)
     */
    @Override
    public Mono<AuthResponseDTO> login(LoginRequestDTO request) {
        String email = request.email();
        log.info("Login attempt in Auth0 for user: {}", email);

        // Step 1: Construct the Resource Owner Password Grant request body
        Map<String, Object> body = Map.of(
            "client_id", auth0Properties.getClientId(),
            "client_secret", auth0Properties.getClientSecret(),
            "grant_type", "http://auth0.com/oauth/grant-type/password-realm",
            "username", email,
            "password", request.password(),
            "realm", "Username-Password-Authentication", // Default Auth0 DB Connection
            "scope", "openid profile email offline_access read:packages read:routes",
            "audience", auth0Properties.getLoginAudience()
        );

        // Step 2: Make POST request to /oauth/token endpoint
        return auth0WebClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            // Step 3: Handle HTTP client error statuses
            .onStatus(status -> status.is4xxClientError(), response -> {
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        return Mono.error(this.handleErrors(errorBody));
                    });
            })
            // Step 4: Deserialize JSON response
            .bodyToMono(JsonNode.class)
            .map(response -> {
                log.debug("Auth0 login successful, parsing response...");
                
                // Step 5: Extract token fields
                String accessToken = response.get("access_token").asText();

                String idToken = response.has("id_token") ? 
                    response.get("id_token").asText() : null;

                String refreshToken = response.has("refresh_token") ? 
                    response.get("refresh_token").asText() : null;
                
                int expiresIn = response.get("expires_in").asInt();

                // Step 6: Decode the Access Token to build user profile claims
                UserInfoDTO userInfo = decodeToken(idToken != null ? idToken : accessToken);

                // Step 7: Return populated response DTO
                return new AuthResponseDTO(accessToken, refreshToken, expiresIn, userInfo);
            });
    }

    /**
     * Obtains a new JWT Access Token using a valid OAuth2 Refresh Token.
     *
     * This method implements the standard OAuth2 Refresh Token Grant flow.
     * It does <b>not</b> require administrative privileges (M2M token); it sends the client credentials
     * along with the active refresh token directly to Auth0's {@code /oauth/token} endpoint.
     *
     * @param request the payload containing the current refresh token
     * @return a {@link Mono} emitting an {@link AuthResponseDTO} populated with the fresh access token and user claims
     * @throws AppException if the refresh token is invalid or expired (401)
     */
    @Override
    public Mono<AuthResponseDTO> refreshToken(RefreshTokenRequestDTO request) {
        log.info("Refreshing access token via Auth0...");

        // Step 1: Build payload for Refresh Token Grant
        Map<String, Object> body = Map.of(
            "client_id", auth0Properties.getClientId(),
            "client_secret", auth0Properties.getClientSecret(),
            "grant_type", "refresh_token",
            "refresh_token", request.refreshToken()
        );

        // Step 2: Make POST request to Auth0 token endpoint
        return auth0WebClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            // Step 3: Handle invalid/expired refresh token errors
            .onStatus(status -> status.is4xxClientError(), response -> {
                log.error("Refresh token failed with status: {}", response.statusCode());
                throw new AppException("Invalid refresh token", 401);
            })
            // Step 4: Deserialize response
            .bodyToMono(JsonNode.class)
            .map(response -> {
                // Step 5: Extract token properties
                String accessToken = response.get("access_token").asText();
        
                String idToken = response.has("id_token") ? 
                    response.get("id_token").asText() : null;

                String newRefreshToken = response.has("refresh_token") 
                    ? response.get("refresh_token").asText() 
                    : request.refreshToken();

                int expiresIn = response.get("expires_in").asInt();

                // Step 6: Decode the Access Token to build user profile claims
                UserInfoDTO userInfo = decodeToken(idToken != null ? idToken : accessToken);

                // Step 7: Return populated DTO
                return new AuthResponseDTO(accessToken, newRefreshToken, expiresIn, userInfo);
            });
    }

    // -- Require auth

    /**
     * Handles user logout and session termination.
     *
     * In a stateless OAuth2 JWT architecture, primary logout is performed client-side
     * by clearing stored Access and Refresh tokens from local storage or application memory.
     *
     * @param request the payload containing the refresh token to invalidate
     * @return an empty {@link Mono} indicating successful operation completion
     */
    @Override
    public Mono<Void> logout(RefreshTokenRequestDTO request) {
        log.info("Processing logout request...");
        
        // Stateless logout: Token removal is handled by the client application.
        return Mono.empty();
    }

    /**
     * Decodes and retrieves user profile information directly from an Authorization Bearer header.
     *
     * This method serves as a utility and validation endpoint to ensure that the API Gateway
     * is correctly propagating the {@code Authorization} header to the downstream microservices.
     * It decodes the JWT locally without making an external HTTP network request to Auth0.
     * @param authHeader the raw HTTP "Authorization" header passed from the Gateway (e.g., {@code "Bearer eyJhbG..."})
     * @return a {@link Mono} emitting a {@link UserInfoDTO} populated with the decoded user claims
     */
    @Override
    public Mono<UserInfoDTO> getUserInfo(String authHeader) {
        log.debug("Fetching user info from Auth0 JWT");

        // Step 1: Sanitize the header by removing the 'Bearer ' prefix
        String token = authHeader.replace("Bearer ", "");

        // Step 2 & 3: Decode the token locally and wrap inside a Mono publisher
        return Mono.just(decodeToken(token));
    }

    // ================================================================
    // -- PRIVATE HELPER METHODS (Management Operations)
    // ================================================================

    /**
     * Obtains a Machine-to-Machine (M2M) Access Token for the Auth0 Management API.
     *
     * @return a {@link Mono} emitting the administrative Bearer Access Token for Auth0
     */
    private Mono<String> getManagementApiToken() {
        // Step 1: Build payload for Client Credentials Grant
        Map<String, Object> body = Map.of(
            "client_id", auth0Properties.getClientId(),
            "client_secret", auth0Properties.getClientSecret(),
            "audience", auth0Properties.getAudience(),
            "grant_type", "client_credentials"
        );

        // Step 2 & 3: Prepare HTTP POST request to /oauth/token
        return auth0WebClient.post()
            .uri("/oauth/token")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            // Step 4: Execute reactive HTTP call
            .retrieve()
            // Step 5: Convert response to JsonNode
            .bodyToMono(JsonNode.class)
            // Step 6: Extract access_token from response
            .map(node -> node.get("access_token").asText());
    }

    /**
     * Creates a new user in Auth0 via the Management API.
     *
     * @param request the registration details containing credentials and profile info
     * @param managementToken the administrative M2M token required for Management API calls
     * @return a {@link Mono} emitting the newly created Auth0 user identifier string
     * @throws AppException if the email is already registered (409) or user creation fails (400)
     */
    private Mono<String> createAuth0User(RegisterRequestDTO request, String managementToken) {

        String firstName = request.firstName() != null ? request.firstName() : "";
        String lastName = request.lastName() != null ? request.lastName() : "";
        String fullName = (firstName + " " + lastName).trim();


        // Step 1: Build the user creation payload
        Map<String, Object> userData = Map.of(
            "client_id", auth0Properties.getClientId(),
            "email", request.email(),
            "password", request.password(),
            "connection", "Username-Password-Authentication",

            // Native Auth0 fields
            "given_name", firstName,
            "family_name", lastName,
            "name", fullName.isEmpty() ? request.email() : fullName,
            // "nickname", ...,
            // "picture", ...,

            // custom fields
            "user_metadata", Map.of(
                "role", "User"
                // "role", request.rol() != null ? request.rol() : "usuario"
            )
        );

        // Step 2 & 3: Post payload with Bearer managementToken
        return auth0WebClient.post()
            // .uri("/api/v2/users")
            .uri("/dbconnections/signup")    // maybe the right endpoint
            .header("Authorization", "Bearer " + managementToken)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(userData)
            .retrieve()

            // Step 4: Handle registration errors
            .onStatus(status -> status.is4xxClientError(), response -> {
                return response.bodyToMono(String.class)
                    .flatMap(errorBody -> {
                        return Mono.error(this.handleErrors(errorBody));
                    });
            })

            // Step 5: Parse response
            .bodyToMono(JsonNode.class)
            // Step 6: Extract created user_id
            // .map(node -> node.get("user_id").asText());
            .map(node -> node.get("_id").asText()); // The signup returns _id, not user_id
    }

    /**
     * Decodes and parses the payload of a Base64URL-encoded Auth0 JSON Web Token (JWT).
     *
     * This helper method performs stateless client-side decoding of the JWT payload without
     * verifying its cryptographic signature ( signature verification is handled by Spring Security
     * / Gateway OAuth2 Resource Server filters). It extracts standard OpenID Connect claims
     * as well as custom Auth0 namespace role claims.
     *
     * @param idToken the raw JWT token string (without the "Bearer " prefix)
     * @return a {@link UserInfoDTO} populated with decoded claims, or an empty DTO if parsing fails
     */
    private UserInfoDTO decodeToken(String idToken) {
        try {
            // Step 1: Decode the ID token (contains user data)
            JsonNode idClaims = parseJwtPayload(idToken);

            // Step 2: Decodificar el access token SOLO para los roles
            JsonNode rolesNode = idClaims.path(Auth0ServiceImpl.claimCustomRoles + "/roles");

            // Step 2: Extract roles from custom Auth0 namespace claim or set default
            List<String> roles = new ArrayList<>();
            if (rolesNode.isArray() && !rolesNode.isEmpty()) {
                rolesNode.forEach(r -> roles.add(r.asText()));
            } else {
                List.of("User", "No Roles");
            }

            // Step 3: Extract standard claims
            String userId = idClaims.path("sub").asText(null);
            String email = idClaims.path("email").asText(null);
            boolean emailVerified = idClaims.path("email_verified").asBoolean(false);

            String name = idClaims.path("name").asText(null);
            String givenName = idClaims.path("given_name").asText(null);
            String familyName = idClaims.path("family_name").asText(null);

            // Si name existe pero no tenemos givenName/familyName de forma explícita
            if (name != null && !name.isBlank()) {
                String[] nameParts = name.trim().split("\\s+", 2);
                
                if (givenName == null || givenName.isBlank()) {
                    givenName = nameParts[0]; // Primer palabra como Nombre
                }
                
                if (familyName == null || familyName.isBlank()) {
                    // Si hay más de una palabra usa la segunda parte como Apellido, si no deja string vacío
                    familyName = nameParts.length > 1 ? nameParts[1] : ""; 
                }
            } else {
                if (givenName == null) givenName = "";
                if (familyName == null) familyName = "";
            }

            // Step 6: Assemble UserInfoDTO
            return new UserInfoDTO(userId, email, email, name, givenName, familyName, emailVerified, roles);

        } catch (Exception e) {
            log.error("Failed to decode tokens: {}", e.getMessage());
            return UserInfoDTO.empty();
        }
    }

    private JsonNode parseJwtPayload(String token) throws Exception {
        // Step 1: Split JWT into Header, Payload, and Signature parts
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new AppException("Invalid token format", 400);
        }

        // Step 2: Decode payload using Base64URL decoder (CRITICAL for JWT compliance)
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
        // String payload = new String(Base64.getDecoder().decode(parts[1]));

        // Step 3: Parse JSON payload
        return objectMapper.readTree(payload);
    }

    // ================================================================
    // -- HANDLE ERRORS FROM API OAUTH0
    // ================================================================

    /**
     * Parses an Auth0 error response body and converts it into an AppException.
     *
     * Auth0 returns errors in two different formats depending on the endpoint:
     * - /oauth/token (login, refresh): { "error": "...", "error_description": "..." }
     * - /dbconnections/signup (register): { "code": "...", "name": "...", "message": "..." }
     *
     * This method extracts the common fields from both formats, builds a list of
     * technical error details for debugging, and maps the error code to a
     * user-friendly message via mapAuth0ErrorToMessage.
     *
     * @param errorBody the raw JSON error body returned by Auth0
     * @return an AppException with a user-friendly message and technical details
     */
    private AppException handleErrors(String errorBody) {
        /* .onStatus(status -> status.is4xxClientError(), response -> {
            if (response.statusCode().value() == 409) {
                throw new AppException("Email already registered in Auth0", 409);
            }
            throw new AppException("Failed to create user in Auth0", 400);
        }) */

        log.error("Auth0 error response: {}", errorBody);

        // 1. Extract common fields from the Auth0 error JSON
        String errorCode = null;
        String errorDesc = null;
        String errorName = null;
        String errorMessage = null;

        List<String> errors = new ArrayList<>();

        try {
            JsonNode json = objectMapper.readTree(errorBody);

            if (json.has("error") && !json.get("error").asText().isBlank()) {
                errorCode = json.get("error").asText();
            }
            if (json.has("error_description") && !json.get("error_description").asText().isBlank()) {
                errorDesc = json.get("error_description").asText();
            }
            if (json.has("code") && !json.get("code").asText().isBlank()) {
                // "code" overrides "error" when present (e.g. signup errors)
                errorCode = json.get("code").asText();
            }
            if (json.has("name") && !json.get("name").asText().isBlank()) {
                errorName = json.get("name").asText();
            }
            if (json.has("message") && !json.get("message").asText().isBlank()) {
                errorMessage = json.get("message").asText();
            }

            // 2. Build the list of technical error details (for debugging)
            for (String field : List.of("error", "error_description", "code", "name", "message")) {
                if (json.has(field) && !json.get(field).asText().isBlank()) {
                    errors.add(field + ": " + json.get(field).asText());
                }
            }

        } catch (Exception e) {
            // If the body is not valid JSON, store the raw string
            errors.add(errorBody);
        }

        if (errors.isEmpty()) {
            errors.add(errorBody);
        }

        // 3. Map the error code to a user-friendly message
        String userMessage = mapAuth0ErrorToMessage(errorCode, errorName, errorDesc, errorMessage);

        return new AppException(userMessage, 400, errors);
    }

    /**
     * Maps an Auth0 error code to a user-friendly message in English.
     *
     * Falls back to the provided description/message, or a generic message
     * if the error code is unknown.
     *
     * @param code    the error code (e.g. "invalid_grant", "user_exists")
     * @param name    the error name from Auth0 (e.g. "PasswordStrengthError")
     * @param desc    the error description, if any
     * @param message the error message, if any
     * @return a user-friendly message ready to be shown to the end user
     */
    private String mapAuth0ErrorToMessage(String code, String name, String desc, String message) {
        // Normalize: prefer "code", fall back to "name", then "unknown"
        String key = code != null ? code : (name != null ? name : "unknown");

        return switch (key) {
            // LOGIN
            case "invalid_grant" ->
                "Invalid credentials. Please check your email and password.";
            case "invalid_request" ->
                "Invalid request. Please check the submitted data.";
            case "unauthorized_client" ->
                "Application not authorized for this flow.";
            case "unsupported_grant_type" ->
                "Unsupported authentication type.";
            case "invalid_client" ->
                "Invalid authentication client.";

            // REGISTER
            case "user_exists" ->
                "This email is already registered.";
            case "PasswordStrengthError" ->
                "The password does not meet the minimum security requirements.";
            case "invalid_password" ->
                "Invalid password.";
            case "invalid_signup" ->
                "Sign up could not be completed. Please check the submitted data.";

            // TOKENS
            case "invalid_token" ->
                "Invalid or expired token. Please sign in again.";
            case "expired_token" ->
                "The token has expired. Please sign in again.";
            case "invalid_refresh_token" ->
                "The refresh token is invalid or expired. Please sign in again.";

            // PERMISSIONS
            case "access_denied" ->
                "Access denied. You do not have permission for this action.";
            case "forbidden" ->
                "You do not have permission for this action.";
            case "insufficient_scope" ->
                "Insufficient permissions for this operation.";

            // GENERIC
            case "server_error" ->
                "Internal authentication server error. Please try again later.";
            case "temporarily_unavailable" ->
                "The authentication service is unavailable. Please try again later.";
            case "too_many_requests" ->
                "Too many attempts. Please wait a moment before trying again.";

            // FALLBACK
            default -> {
                if (desc != null && !desc.isBlank()) yield "Authentication error: " + desc;
                if (message != null && !message.isBlank()) yield "Authentication error: " + message;
                yield "Authentication error. Please try again.";
            }
        };
    }
}