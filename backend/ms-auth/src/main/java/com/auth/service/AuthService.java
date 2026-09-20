package com.auth.service;

import com.auth.dto.request.LoginRequestDTO;
import com.auth.dto.request.RefreshTokenRequestDTO;
import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import reactor.core.publisher.Mono;

/**
 * Interface representing the core authentication contract for the application.
 *
 * <p>Follows the Dependency Inversion Principle (DIP) to decouple controllers
 * and downstream components from specific Identity Provider (IdP) implementations
 * (e.g., Keycloak, Auth0, or custom JWT providers).</p>
 *
 * <p>All methods are fully reactive and return Project Reactor {@link Mono} publishers.</p>
 */
public interface AuthService {

    /**
     * Registers a new user within the underlying Identity Provider and logs them in.
     *
     * @param request the registration details including user credentials and personal info
     * @return a {@link Mono} emitting an {@link AuthResponseDTO} with tokens and user details
     */
    Mono<AuthResponseDTO> registerUser(RegisterRequestDTO request);

    /**
     * Authenticates a user using their credentials.
     *
     * @param request the login payload containing user email and password
     * @return a {@link Mono} emitting an {@link AuthResponseDTO} with access and refresh tokens
     */
    Mono<AuthResponseDTO> login(LoginRequestDTO request);

    /**
     * Obtains a fresh access token using a valid refresh token.
     *
     * @param request the payload containing the active refresh token
     * @return a {@link Mono} emitting a new {@link AuthResponseDTO} with the refreshed access token
     */
    Mono<AuthResponseDTO> refreshToken(RefreshTokenRequestDTO request);

    /**
     * Invalidates the specified refresh token and terminates the session in the Provider.
     *
     * @param request the payload containing the refresh token to invalidate
     * @return a {@link Mono} emitting {@code Void} upon successful completion
     */
    Mono<Void> logout(RefreshTokenRequestDTO request);

    /**
     * Decodes and retrieves user profile information from an Authorization header.
     *
     * @param authHeader the raw HTTP "Authorization" header containing the Bearer token
     * @return a {@link Mono} emitting a {@link UserInfoDTO} populated with extracted claims
     */
    Mono<UserInfoDTO> getUserInfo(String authHeader);
}