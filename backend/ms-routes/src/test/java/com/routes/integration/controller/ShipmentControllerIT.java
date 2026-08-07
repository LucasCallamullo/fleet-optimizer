package com.routes.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.routes.client.FleetClient;
import com.routes.client.GeocodingClient;
import com.routes.client.PackageClient;
import com.routes.dto.client.fleets.FleetVehicleDTO;
import com.routes.dto.client.geocoding.BatchDistanceResponse;
import com.routes.dto.client.geocoding.DistanceResult;
import com.routes.dto.client.packages.PackageDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.dto.request.ShipmentRequestDTO;
import com.routes.repository.LegRepository;
import com.routes.repository.RouteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Shipment Controller Integration Tests")
class ShipmentControllerIT {

    // ================================================================
    // DEPENDENCIES
    // ================================================================

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RouteRepository routeRepository;

    @Autowired
    private LegRepository legRepository;

    // ================================================================
    // MOCKED FEIGN CLIENTS (NO otros MS reales)
    // ================================================================

    @MockBean
    private PackageClient packageClient;

    @MockBean
    private FleetClient fleetClient;

    @MockBean
    private GeocodingClient geocodingClient;

    // ================================================================
    // TEST DATA
    // ================================================================

    private ShipmentRequestDTO request;

    @BeforeEach
    void setUp() {
        // Step 1: Clean database
        legRepository.deleteAll();
        routeRepository.deleteAll();

        // Step 2: Create destination
        LocationRequestDTO destination = new LocationRequestDTO(
            "Av. Colon", "500", "Cordoba", "Cordoba",
            "Argentina", "5000", -31.4201, -64.1888
        );

        // Step 3: Create request
        request = new ShipmentRequestDTO(
            List.of(1L, 2L),
            1L,
            destination
        );
    }

    // ================================================================
    // TEST 1: SUCCESS (with authentication)
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/shipments - Should create shipment")
    void shouldCreateShipment() throws Exception {
        // Step 1: Mock Feign Client responses
        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(mockPackages());
        when(fleetClient.getVehicleById(anyLong()))
            .thenReturn(mockVehicle());
        when(geocodingClient.calculateDistances(any()))
            .thenReturn(mockGeocodingResponse());

        // Step 2: Execute request
        mockMvc.perform(post("/api/v1/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))

                // Step 3: Assert
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.routeId").exists())
                .andExpect(jsonPath("$.vehicleId").value(1L))
                .andExpect(jsonPath("$.legs").isArray());
    }

    // ================================================================
    // TEST 2: UNAUTHORIZED (no authentication)
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/shipments - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(post("/api/v1/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // TEST 3: VALIDATION ERROR (empty packages)
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("POST /api/v1/shipments - Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        ShipmentRequestDTO invalidRequest = new ShipmentRequestDTO(
            List.of(),  // ← Empty list (should fail)
            1L,
            null       // ← Null destination (should fail)
        );

        mockMvc.perform(post("/api/v1/shipments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").isString());
    }

    // ================================================================
    // HELPER METHODS (Mock data)
    // ================================================================

    private List<PackageDTO> mockPackages() {
        return List.of(
            new PackageDTO(1L, 10.0, 0.30, null),
            new PackageDTO(2L, 20.0, 0.50, null)
        );
    }

    private FleetVehicleDTO mockVehicle() {
        return new FleetVehicleDTO(
            1L, "ABC123", 10.5, 1.50, 2.50, 100.0, 50.0, "AVAILABLE"
        );
    }

    private BatchDistanceResponse mockGeocodingResponse() {
        return new BatchDistanceResponse(List.of(
            new DistanceResult(0L, 700.0, 480),
            new DistanceResult(1L, 700.0, 480)
        ));
    }
}