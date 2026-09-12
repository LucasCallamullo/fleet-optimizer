package com.geocoding.integration;


import com.geocoding.dto.common.LocationDTO;
import com.geocoding.dto.request.BatchDistanceRequestDTO;
import com.geocoding.dto.request.DistanceRequestDTO;
import com.geocoding.dto.request.LocationPairDTO;
import com.geocoding.dto.response.BatchDistanceResponseDTO;
import com.geocoding.dto.response.DistanceResponseDTO;
import com.geocoding.dto.response.DistanceResultDTO;
import com.geocoding.exception.AppException;
import com.geocoding.service.OrsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayName("GeocodingController - Integration Tests")
class GeocodingControllerIT {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrsService orsService;

    // ================================================================
    // POST /api/v1/distance
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/distance - Should return wrapped 200 response")
    @WithMockUser(roles = "USER")
    void shouldReturnWrappedDistance() {
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
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.distanceKm").isEqualTo(700.5)
                .jsonPath("$.data.durationMinutes").isEqualTo(480);
    }

    @Test
    @DisplayName("POST /api/v1/distance - Should return 400 with ErrorResponse when body is invalid")
    @WithMockUser(roles = "USER")
    void shouldReturn400WithErrorResponse() {
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
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.path").isEqualTo("/api/v1/distance");
    }

    @Test
    @DisplayName("POST /api/v1/distance - Should return 500 with ErrorResponse when ORS fails")
    @WithMockUser(roles = "USER")
    void shouldReturn500WhenOrsFails() {
        when(orsService.calculateDistance(any()))
                .thenReturn(Mono.error(new AppException("Error calculating distance", 500)));

        DistanceRequestDTO request = new DistanceRequestDTO(
                -34.6037, -58.3816,
                -31.4201, -64.1888
        );

        webTestClient.post()
                .uri("/api/v1/distance")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.status").isEqualTo(500)
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.detail").isEqualTo("Error calculating distance");
    }

    @Test
    @DisplayName("POST /api/v1/distance - Should return 401 without authentication")
    void shouldReturn401WithoutAuth() {
        DistanceRequestDTO request = new DistanceRequestDTO(
                -34.6037, -58.3816,
                -31.4201, -64.1888
        );

        webTestClient.post()
                .uri("/api/v1/distance")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.detail").isEqualTo("Unauthorized");
    }

    // ================================================================
    // POST /api/v1/distance/batch
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/distance/batch - Should return wrapped 200 response")
    @WithMockUser(roles = "USER")
    void shouldReturnWrappedBatchDistances() {
        List<DistanceResultDTO> results = List.of(
                new DistanceResultDTO(1L, 700.5, 480),
                new DistanceResultDTO(2L, 650.3, 420)
        );
        when(orsService.calculateBatchDistances(any()))
                .thenReturn(Mono.just(new BatchDistanceResponseDTO(results)));

        BatchDistanceRequestDTO request = new BatchDistanceRequestDTO(List.of(
                new LocationPairDTO(1L,
                        LocationDTO.ofCoordinates(-34.6037, -58.3816),
                        LocationDTO.ofCoordinates(-31.4201, -64.1888)),
                new LocationPairDTO(2L,
                        LocationDTO.ofCoordinates(-31.4201, -64.1888),
                        LocationDTO.ofCoordinates(-32.8908, -68.8272))
        ));

        webTestClient.post()
                .uri("/api/v1/distance/batch")
                .header("X-User-Id", "user-123")
                .header("X-User-Roles", "USER")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo(200)
                .jsonPath("$.data.results.length()").isEqualTo(2)
                .jsonPath("$.data.results[0].legId").isEqualTo(1)
                .jsonPath("$.data.results[0].distanceKm").isEqualTo(700.5)
                .jsonPath("$.data.results[1].legId").isEqualTo(2);
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
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.success").isEqualTo(false);
    }
}