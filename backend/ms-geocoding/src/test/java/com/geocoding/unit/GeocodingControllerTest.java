package com.geocoding.unit;

import com.geocoding.dto.request.BatchDistanceRequestDTO;
import com.geocoding.dto.request.DistanceRequestDTO;
import com.geocoding.config.CustomAccessDeniedHandler;
import com.geocoding.config.CustomAuthenticationEntryPoint;
import com.geocoding.config.SecurityConfig;
import com.geocoding.controller.GeocodingController;
import com.geocoding.dto.common.LocationDTO;
import com.geocoding.dto.request.LocationPairDTO;
import com.geocoding.dto.response.BatchDistanceResponseDTO;
import com.geocoding.dto.response.DistanceResponseDTO;
import com.geocoding.dto.response.DistanceResultDTO;
import com.geocoding.service.OrsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;


import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = GeocodingController.class)
@Import(SecurityConfig.class)
class GeocodingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrsService orsService;

    @MockBean
    private CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    // @MockBean
    // private GatewayAuthenticationFilter gatewayAuthenticationFilter;

    // ================================================================
    // POST /api/v1/distance
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/distance - Should return 200 with distance data")
    @WithMockUser(roles = "USER")
    void shouldCalculateDistance() {
        // Arrange
        DistanceResponseDTO mockResponse = new DistanceResponseDTO(700.5, 480, "{}");
            when(orsService.calculateDistance(any())).thenReturn(Mono.just(mockResponse));

        DistanceRequestDTO request = new DistanceRequestDTO(
                -34.6037, -58.3816,
                -31.4201, -64.1888
        );

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/distance")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.distanceKm").isEqualTo(700.5)
                .jsonPath("$.durationMinutes").isEqualTo(480);
    }

    @Test
    @DisplayName("POST /api/v1/distance - Should return 400 when latitude is missing")
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenCoordinatesMissing() {
        // Missing destLat -> @Valid must reject before reaching the service
        String invalidBody = """
                {
                    "originLat": -34.6037,
                    "originLon": -58.3816,
                    "destLat": null,
                    "destLon": -64.1888
                }
                """;

        webTestClient.post()
                .uri("/api/v1/distance")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    // ================================================================
    // POST /api/v1/distance/batch
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/distance/batch - Should return 200 with results for each leg")
    @WithMockUser(roles = "USER")
    void shouldCalculateBatchDistances() {
        var a = new DistanceResultDTO(1L, 700.5, 480);
        var b = new DistanceResultDTO(2L, 650.3, 420);

        // Arrange
        List<DistanceResultDTO> results = List.of(a, b);
        BatchDistanceResponseDTO mockResponse = new BatchDistanceResponseDTO(results);
            when(orsService.calculateBatchDistances(any())).thenReturn(Mono.just(mockResponse));


        BatchDistanceRequestDTO request = new BatchDistanceRequestDTO(List.of(
                new LocationPairDTO(1L,
                        LocationDTO.ofCoordinates(-34.6037, -58.3816),
                        LocationDTO.ofCoordinates(-31.4201, -64.1888)),
                new LocationPairDTO(2L,
                        LocationDTO.ofCoordinates(-31.4201, -64.1888),
                        LocationDTO.ofCoordinates(-32.8908, -68.8272))
        ));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/distance/batch")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.results.length()").isEqualTo(2)
                .jsonPath("$.results[0].legId").isEqualTo(1)
                .jsonPath("$.results[0].distanceKm").isEqualTo(700.5)
                .jsonPath("$.results[1].legId").isEqualTo(2);
    }

    @Test
    @DisplayName("POST /api/v1/distance/batch - Should return 400 when locations list is empty")
    @WithMockUser(roles = "USER")
    void shouldReturn400WhenBatchEmpty() {
        String invalidBody = """
                { "locations": [] }
                """;

        webTestClient.post()
                .uri("/api/v1/distance/batch")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidBody)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
