package com.auth.integration.controller;

import com.auth.dto.request.LoginRequestDTO;
import com.auth.dto.request.RegisterRequestDTO;
import com.auth.dto.response.AuthResponseDTO;
import com.auth.dto.response.UserInfoDTO;
import com.auth.exception.AppException;
import com.auth.service.KeycloakService;
// import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Auth Controller Integration Tests")
class AuthControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    // @Autowired
    // private ObjectMapper objectMapper;

    @MockBean
    private KeycloakService keycloakService;

    // ================================================================
    // TEST: LOGIN - SUCCESS
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 200 with tokens")
    void shouldLoginSuccessfully() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "password123");

        UserInfoDTO userInfo = new UserInfoDTO(
            "user-123",
            "user_regular",
            "user@example.com",
            "User Regular",
            "User",
            "Regular",
            false,
            List.of("USER")
        );

        AuthResponseDTO mockResponse = new AuthResponseDTO(
            "access.token.here",
            "refresh.token.here",
            300,
            userInfo
        );

        when(keycloakService.login(anyString(), anyString()))
            .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.accessToken").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("access.token.here"))
                .jsonPath("$.refreshToken").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("refresh.token.here"))
                .jsonPath("$.user.id").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("user-123"));
    }

    // ================================================================
    // TEST: LOGIN - INVALID CREDENTIALS
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/login - Should return 401 when invalid credentials")
    void shouldReturn401WhenInvalidCredentials() {
        LoginRequestDTO request = new LoginRequestDTO("user@example.com", "wrongpassword");

        when(keycloakService.login(anyString(), anyString()))
            .thenThrow(new AppException("Invalid credentials", 401));

        webTestClient.post()
                .uri("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((Integer) msg).isEqualTo(401))
                .jsonPath("$.error").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("Invalid credentials"));
    }

    // ================================================================
    // TEST: REGISTER - SUCCESS
    // ================================================================

    /* 
    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 201 when registered")
    void shouldRegisterSuccessfully() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "newuser@example.com",
                "password123",
                "New",
                "User",
                "usuario"
        );

        UserInfoDTO userInfo = new UserInfoDTO(
            "user-456",
            "newuser",
            "newuser@example.com",
            "New User",
            "New",
            "User",
            false,
            List.of("USER")
        );

        AuthResponseDTO mockResponse = new AuthResponseDTO(
            "access.token.new",
            "refresh.token.new",
            300,
            userInfo
        );

        when(keycloakService.registerUser(any(RegisterRequestDTO.class)))
            .thenReturn(Mono.just(mockResponse));

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.accessToken").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("access.token.new"))
                .jsonPath("$.user.email").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("newuser@example.com"));
    }  */

    // ================================================================
    // TEST: REGISTER - DUPLICATE EMAIL
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/register - Should return 409 when email already exists")
    void shouldReturn409WhenEmailAlreadyExists() {
        RegisterRequestDTO request = new RegisterRequestDTO(
                "existing@example.com",
                "password123",
                "Existing",
                "User",
                "usuario"
        );

        when(keycloakService.registerUser(any(RegisterRequestDTO.class)))
            .thenThrow(new AppException("Email already registered", 409));

        webTestClient.post()
                .uri("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.status").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((Integer) msg).isEqualTo(409))
                .jsonPath("$.error").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("Email already registered"));
    }

    // ================================================================
    // TEST: REFRESH TOKEN
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/auth/refresh - Should return 200 with new token")
    void shouldRefreshToken() {
        // Similar a login
    }

    // ================================================================
    // TEST: LOGOUT
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/auth/logout - Should return 200 on logout")
    void shouldLogout() {
        when(keycloakService.logout(anyString())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/v1/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"refreshToken\":\"token\"}")
                .exchange()
                .expectStatus().isOk();
    }

    // ================================================================
    // TEST: PROFILE
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/auth/profile - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticatedForProfile() {
        webTestClient.get()
                .uri("/api/v1/auth/profile")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
