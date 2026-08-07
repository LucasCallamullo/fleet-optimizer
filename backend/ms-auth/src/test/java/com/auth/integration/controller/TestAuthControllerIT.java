package com.auth.integration.controller;

import com.auth.dto.response.UserInfoDTO;
import com.auth.service.KeycloakService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Test Auth Controller Integration Tests")
class TestAuthControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean  // ← Mock de KeycloakService
    private KeycloakService keycloakService;

    // ================================================================
    // PUBLIC ENDPOINTS (no authentication required)
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/auth/test/public - Should return 200 without authentication")
    void shouldReturnPublicEndpoint() {
        webTestClient.get()
                .uri("/api/v1/auth/test/public")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("public"));
    }

    @Test
    @DisplayName("GET /api/v1/auth/test/info - Should return 200 without authentication")
    void shouldReturnInfoEndpoint() {
        webTestClient.get()
                .uri("/api/v1/auth/test/info")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.base_url").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("/api/v1/auth/test"));
    }

    // ================================================================
    // PROTECTED ENDPOINT - AUTHENTICATED
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/auth/test/authenticated - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() {
        webTestClient.get()
                .uri("/api/v1/auth/test/authenticated")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/auth/test/authenticated - Should return 200 when authenticated")
    void shouldReturn200WhenAuthenticated() {
        // ✅ No necesita mock porque el controller no llama a KeycloakService
        // Solo usa los headers X-User-Id y X-User-Roles
        webTestClient.get()
                .uri("/api/v1/auth/test/authenticated")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.success").isBoolean()
                .jsonPath("$.message").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("authenticated"));
    }

    // ================================================================
    // USER ROLE ENDPOINT
    // ================================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/auth/test/user - Should return 200 when USER role")
    void shouldReturn200WhenUserRole() {
        webTestClient.get()
                .uri("/api/v1/auth/test/user")
                .header("X-User-Id", "user-123")
                // necesario agregar todos los headers al parecer para funcionar el test
                .header("X-User-Email", "user@gmail.com")
                .header("X-User-Roles", "USER")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("users (and admins too)"));
    }

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("GET /api/v1/auth/test/user - Should return 403 when wrong role")
    void shouldReturn403WhenWrongRole() {
        webTestClient.get()
                .uri("/api/v1/auth/test/user")
                .header("X-User-Id", "guest-123")
                .header("X-User-Roles", "GUEST")
                .exchange()
                .expectStatus().isForbidden();
    }

    // ================================================================
    // ADMIN ROLE ENDPOINT
    // ================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/auth/test/admin - Should return 200 when ADMIN role")
    void shouldReturn200WhenAdminRolee() {
        webTestClient.get()
                .uri("/api/v1/auth/test/admin")
                .header("X-User-Id", "admin-123")
                // necesario agregar todos los headers al parecer para funcionar el test
                .header("X-User-Email", "user@gmail.com")
                .header("X-User-Roles", "ADMIN")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("Administrator"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/auth/test/admin - Should return 403 when USER role")
    void shouldReturn403WhenUserRole() {
        webTestClient.get()
                .uri("/api/v1/auth/test/admin")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .exchange()
                .expectStatus().isForbidden();
    }

    // ================================================================
    // MULTI-ROLE ENDPOINT
    // ================================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /api/v1/auth/test/multi-role - Should return 200 when USER role")
    void shouldReturn200WhenUserRoleForMultiRole() {
        webTestClient.get()
                .uri("/api/v1/auth/test/multi-role")
                .header("X-User-Id", "user-123")
                // necesario agregar todos los headers al parecer para funcionar el test
                .header("X-User-Email", "user@gmail.com")
                .header("X-User-Roles", "USER")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.message").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).contains("users and administrators"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/auth/test/multi-role - Should return 200 when ADMIN role")
    void shouldReturn200WhenAdminRoleForMultiRole() {
        webTestClient.get()
                .uri("/api/v1/auth/test/multi-role")
                .header("X-User-Id", "admin-123")
                // necesario agregar todos los headers al parecer para funcionar el test
                .header("X-User-Email", "user@gmail.com")
                .header("X-User-Roles", "ADMIN")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowedRoles").isArray();
    }

    @Test
    @WithMockUser(roles = "GUEST")
    @DisplayName("GET /api/v1/auth/test/multi-role - Should return 403 when wrong role")
    void shouldReturn403WhenWrongRoleForMultiRole() {
        webTestClient.get()
                .uri("/api/v1/auth/test/multi-role")
                .header("X-User-Id", "guest-123")
                .header("X-User-Roles", "GUEST")
                .exchange()
                .expectStatus().isForbidden();
    }

    // ================================================================
    // PROFILE ENDPOINT - REQUIERE MOCK DE KeycloakService
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/auth/test/profile - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticatedForProfile() {
        webTestClient.get()
                .uri("/api/v1/auth/test/profile")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/auth/test/profile - Should return 200 with user info")
    void shouldReturnProfileWhenAuthenticated() {
        // ✅ Mock de KeycloakService para este test
        UserInfoDTO mockUserInfo = new UserInfoDTO(
            "user-123",
            "user_regular",
            "user@example.com",
            "User Regular",
            "User",
            "Regular",
            false,
            List.of("USER")
        );

        when(keycloakService.getUserInfo(anyString()))
            .thenReturn(Mono.just(mockUserInfo));

        webTestClient.get()
                .uri("/api/v1/auth/test/profile")
                .header("Authorization", "Bearer valid.token.here")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("user-123"))
                .jsonPath("$.email").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("user@example.com"));
    }

    // ================================================================
    // WITHOUT REQUIRED HEADERS
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/auth/test/authenticated - Should return 200 even without X-User-Id header")
    void shouldReturn200WithoutUserIdHeader() {
        webTestClient.get()
                .uri("/api/v1/auth/test/authenticated")
                // ❌ Sin header X-User-Id
                // necesario agregar todos los headers al parecer para funcionar el test
                .header("X-User-Email", "user@gmail.com")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.user.id").value(msg -> 
                    org.assertj.core.api.Assertions.assertThat((String) msg).isEqualTo("N/A"));
    }

    // ================================================================
    // PROTECTED ENDPOINT - WITHOUT AUTHENTICATION (401)
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/auth/test/authenticated - Should return 401 without authentication")
    void shouldReturn401WithoutAuthentication() {
        webTestClient.get()
                .uri("/api/v1/auth/test/authenticated")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}