package com.auth.integration.controller;

import com.auth.dto.request.LoginRequestDTO;
import com.auth.dto.request.RefreshTokenRequestDTO;
import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import com.auth.exception.AppException;
import com.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    // Mock the INTERFACE, not the implementation.
    // Spring will inject this mock wherever AuthService is required.
    @MockBean(name = "auth0Service")
    private AuthService authService;

    // ================================================================
    // FIXTURES
    // ================================================================

    /**
     * Builds a UserInfoDTO with the fields the controller returns.
     * Adjust the constructor params to match your actual UserInfoDTO record.
     */
    private UserInfoDTO buildUserInfo() {
        return new UserInfoDTO(
            "auth0|6aaf150a5456d6f4d324b700",
            "user_regular",
            "user@example.com",
            "User Regular",
            "User",
            "Regular",
            true,
            List.of("User")
        );
    }

    /**
     * Builds a realistic AuthResponseDTO.
     * Uses fake but well-formed JWT-like strings (3 parts separated by dots).
     */
    private AuthResponseDTO buildAuthResponse() {
        String accessToken = "eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhdXRoMHxhYmMifQ.signature";
        String refreshToken = "refresh-token-opaque-value";
        return new AuthResponseDTO(accessToken, refreshToken, 86400, buildUserInfo());
    }

    // ================================================================
    // TEST: LOGIN - SUCCESS
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 200 with tokens")
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "password123");
        AuthResponseDTO mockResponse = buildAuthResponse();

        when(authService.login(any(LoginRequestDTO.class)))
            .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(200)
            .jsonPath("$.data.accessToken").isEqualTo(mockResponse.accessToken())
            .jsonPath("$.data.refreshToken").isEqualTo(mockResponse.refreshToken())
            .jsonPath("$.data.user.id").isEqualTo(mockResponse.user().id())
            .jsonPath("$.data.user.email").isEqualTo(mockResponse.user().email())
            .jsonPath("$.data.user.roles[0]").isEqualTo("User");
    }

    // ================================================================
    // TEST: LOGIN - INVALID CREDENTIALS
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 401 when invalid credentials")
    void shouldReturn401WhenInvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "wrongpassword");

        when(authService.login(any(LoginRequestDTO.class)))
            .thenReturn(Mono.error(new AppException(
                "Invalid credentials. Please check your email and password.",
                401,
                List.of("error: invalid_grant", "error_description: Wrong email or password.")
            )));

        webTestClient.post()
            .uri("/api/v1/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .jsonPath("$.status").isEqualTo(401)
            .jsonPath("$.success").isEqualTo(false)
            .jsonPath("$.detail").isEqualTo("Invalid credentials. Please check your email and password.")
            .jsonPath("$.errors[0]").isEqualTo("error: invalid_grant");
    }

    // ================================================================
    // TEST: REGISTER - SUCCESS
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 201 when registered")
    void shouldRegisterSuccessfully() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "newuser@example.com",
            "Password1234567",
            "New",
            "User",
            "User"
        );
        AuthResponseDTO mockResponse = buildAuthResponse();

        when(authService.registerUser(any(RegisterRequestDTO.class)))
            .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.status").isEqualTo(201)
            .jsonPath("$.data.accessToken").isEqualTo(mockResponse.accessToken())
            .jsonPath("$.data.user.email").isEqualTo(mockResponse.user().email());
    }

    // ================================================================
    // TEST: REGISTER - WEAK PASSWORD
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 400 when password is weak")
    void shouldReturn400WhenPasswordIsWeak() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "newuser@example.com",
            "weak",
            "New",
            "User",
            "User"
        );

        when(authService.registerUser(any(RegisterRequestDTO.class)))
            .thenReturn(Mono.error(new AppException(
                "The password does not meet the minimum security requirements.",
                400,
                List.of("code: invalid_password", "name: PasswordStrengthError")
            )));

        webTestClient.post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody()
            .jsonPath("$.status").isEqualTo(400);
            // .jsonPath("$.errors").isArray()
            // .jsonPath("$.errors.length()").isEqualTo(2);
    }

    // ================================================================
    // TEST: REGISTER - DUPLICATE EMAIL
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO(
            "existing@example.com",
            "Password1234567",
            "Existing",
            "User",
            "User"
        );

        when(authService.registerUser(any(RegisterRequestDTO.class)))
            .thenReturn(Mono.error(new AppException(
                "This email is already registered.",
                409,
                List.of("code: user_exists", "message: User already exists")
            )));

        webTestClient.post()
            .uri("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.status").isEqualTo(409)
            .jsonPath("$.detail").isEqualTo("This email is already registered.");
    }

    // ================================================================
    // TEST: REFRESH - SUCCESS
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 200 with new access token")
    void shouldRefreshTokenSuccessfully() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("valid-refresh-token");
        AuthResponseDTO mockResponse = buildAuthResponse();

        when(authService.refreshToken(any(RefreshTokenRequestDTO.class)))
            .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
            .uri("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(200)
            .jsonPath("$.data.accessToken").isEqualTo(mockResponse.accessToken());
    }

    // ================================================================
    // TEST: REFRESH - INVALID TOKEN
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 401 when refresh token is invalid")
    void shouldReturn401WhenRefreshTokenIsInvalid() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("expired-token");

        when(authService.refreshToken(any(RefreshTokenRequestDTO.class)))
            .thenReturn(Mono.error(new AppException(
                "The refresh token is invalid or expired. Please sign in again.",
                401,
                List.of("error: invalid_grant")
            )));

        webTestClient.post()
            .uri("/api/v1/auth/refresh")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isUnauthorized()
            .expectBody()
            .jsonPath("$.status").isEqualTo(401)
            .jsonPath("$.detail").isEqualTo("The refresh token is invalid or expired. Please sign in again.");
    }

    // ================================================================
    // TEST: LOGOUT - SUCCESS
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/auth/logout - Should return 204 on logout")
    void shouldLogoutSuccessfully() {
        RefreshTokenRequestDTO request = new RefreshTokenRequestDTO("refresh-token-to-invalidate");

        when(authService.logout(any(RefreshTokenRequestDTO.class)))
            .thenReturn(Mono.empty());

        webTestClient.post()
            .uri("/api/v1/auth/logout")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .exchange()
            .expectStatus().isNoContent();
    }

    // ================================================================
    // TEST: PROFILE - UNAUTHENTICATED
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/auth/profile - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticatedForProfile() {
        webTestClient.get()
            .uri("/api/v1/auth/profile")
            .exchange()
            .expectStatus().isUnauthorized();
    }

    // ================================================================
    // TEST: PROFILE - AUTHENTICATED
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/auth/profile - Should return 200 with user info")
    void shouldReturnProfileWhenAuthenticated() {
        UserInfoDTO mockUser = buildUserInfo();
        String fakeJwt = "Bearer eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhdXRoMHxhYmMifQ.signature";

        when(authService.getUserInfo(any(String.class)))
            .thenReturn(Mono.just(mockUser));

        webTestClient.get()
            .uri("/api/v1/auth/profile")
            .header("Authorization", fakeJwt)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.status").isEqualTo(200)
            .jsonPath("$.data.id").isEqualTo(mockUser.id())
            .jsonPath("$.data.email").isEqualTo(mockUser.email());
    }
}