package com.geocoding.unit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocoding.dto.common.LocationDTO;
import com.geocoding.dto.request.BatchDistanceRequestDTO;
import com.geocoding.dto.request.DistanceRequestDTO;
import com.geocoding.dto.request.LocationPairDTO;
import com.geocoding.exception.AppException;
import com.geocoding.service.OrsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("OrsService - Unit Tests")
class OrsServiceTest {

    private OrsService orsService;
    private ExchangeFunction exchangeFunction;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        exchangeFunction = mock(ExchangeFunction.class);
        WebClient webClient = WebClient.builder()
                .exchangeFunction(exchangeFunction)
                .baseUrl("http://localhost")
                .build();

        objectMapper = new ObjectMapper();
        orsService = new OrsService(webClient);

        // Inject the @Value-injected apiKey since it is not resolved outside Spring.
        ReflectionTestUtils.setField(orsService, "apiKey", "test-api-key");
    }

    // ================================================================
    // calculateDistance
    // ================================================================

    @Test
    @DisplayName("calculateDistance - Should parse ORS GeoJSON and convert units")
    void shouldParseOrsResponse() throws Exception {
        // Arrange: simulate a valid ORS GeoJSON response.
        String orsJson = """
                {
                  "features": [{
                    "geometry": {
                      "type": "LineString",
                      "coordinates": [[-58.38, -34.60], [-64.18, -31.42]]
                    },
                    "properties": {
                      "summary": { "distance": 700500.0, "duration": 28800.0 }
                    }
                  }]
                }
                """;
        JsonNode node = objectMapper.readTree(orsJson);

        ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(node.toString())
                .build();

        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(response));

        DistanceRequestDTO request = new DistanceRequestDTO(
                -34.6037, -58.3816,
                -31.4201, -64.1888
        );

        // Act & Assert
        StepVerifier.create(orsService.calculateDistance(request))
                .assertNext(dto -> {
                    assertThat(dto.distanceKm()).isEqualTo(700.5);
                    assertThat(dto.durationMinutes()).isEqualTo(480);
                    assertThat(dto.geometry()).contains("LineString");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("calculateDistance - Should throw AppException when ORS returns 500")
    void shouldThrowWhenOrsReturnsError() {
        ClientResponse errorResponse = ClientResponse
                .create(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"ORS is down\"}")
                .build();

        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(errorResponse));

        DistanceRequestDTO request = new DistanceRequestDTO(
                -34.6037, -58.3816,
                -31.4201, -64.1888
        );

        StepVerifier.create(orsService.calculateDistance(request))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(AppException.class);
                    assertThat(err.getMessage()).contains("Error calculating distance");
                    assertThat(((AppException) err).getStatusCode()).isEqualTo(500);
                })
                .verify();
    }

    // ================================================================
    // calculateBatchDistances
    // ================================================================

    @Test
    @DisplayName("calculateBatchDistances - Should parse matrix response and map legIds")
    void shouldParseMatrixResponse() throws Exception {
        // Arrange: ORS Matrix returns distances/durations as 2D arrays.
        String matrixJson = """
                {
                  "distances": [[700500.0, 650300.0]],
                  "durations": [[28800.0, 25200.0]]
                }
                """;
        JsonNode node = objectMapper.readTree(matrixJson);

        ClientResponse response = ClientResponse
                .create(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(node.toString())
                .build();

        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(response));

        BatchDistanceRequestDTO request = new BatchDistanceRequestDTO(List.of(
                new LocationPairDTO(1L,
                        LocationDTO.ofCoordinates(-34.6037, -58.3816),
                        LocationDTO.ofCoordinates(-31.4201, -64.1888))
        ));

        // Act & Assert
        StepVerifier.create(orsService.calculateBatchDistances(request))
                .assertNext(dto -> {
                    assertThat(dto.results()).hasSize(1);
                    assertThat(dto.results().get(0).legId()).isEqualTo(1L);
                    assertThat(dto.results().get(0).distanceKm()).isEqualTo(700.5);
                    assertThat(dto.results().get(0).durationMinutes()).isEqualTo(480);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("calculateBatchDistances - Should throw AppException when ORS returns 500")
    void shouldThrowWhenMatrixOrsReturnsError() {
        ClientResponse errorResponse = ClientResponse
                .create(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body("{\"error\":\"ORS matrix is down\"}")
                .build();

        when(exchangeFunction.exchange(any())).thenReturn(Mono.just(errorResponse));

        BatchDistanceRequestDTO request = new BatchDistanceRequestDTO(List.of(
                new LocationPairDTO(1L,
                        LocationDTO.ofCoordinates(-34.6037, -58.3816),
                        LocationDTO.ofCoordinates(-31.4201, -64.1888))
        ));

        StepVerifier.create(orsService.calculateBatchDistances(request))
                .expectErrorSatisfies(err -> {
                    assertThat(err).isInstanceOf(AppException.class);
                    assertThat(err.getMessage()).contains("Error calculating batch distances");
                    assertThat(((AppException) err).getStatusCode()).isEqualTo(500);
                })
                .verify();
    }
}
