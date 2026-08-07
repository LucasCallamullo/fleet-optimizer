package com.packages.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.request.PackageStatusUpdateRequest;
import com.packages.model.embedded.Location;
import com.packages.model.entity.Package;
import com.packages.model.entity.Store;
import com.packages.model.enums.PackageStatus;
import com.packages.repository.PackageRepository;
import com.packages.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Package Controller Integration Tests")
class PackageControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private StoreRepository storeRepository;

    private Store store;
    private Package testPackage;

    @BeforeEach
    void setUp() {
        // Clean database
        packageRepository.deleteAll();
        storeRepository.deleteAll();

        // Create store
        Location location = new Location();
        location.setStreet("Av. Libertador");
        location.setStreetNumber("1000");
        location.setCity("Buenos Aires");
        location.setCountry("Argentina");
        location.setLatitude(-34.6037);
        location.setLongitude(-58.3816);

        store = new Store();
        store.setName("Test Store");
        store.setLocation(location);
        store.setOwnerId("admin-id");
        store = storeRepository.save(store);
        assertThat(store.getId()).isNotNull();  // ← Verificar que tiene ID

        // Create package
        testPackage = new Package();
        testPackage.setTrackingNumber("PKG-001");
        testPackage.setTotalWeightKg(10.0);
        testPackage.setTotalVolumeCbm(0.30);
        testPackage.setStore(store);
        testPackage.setOwnerId("user-id");
        testPackage.setStatus(PackageStatus.CREATED);
        testPackage = packageRepository.save(testPackage);
    }

    // ================================================================
    // TEST 1: GET ALL PACKAGES - Requires authentication
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/packages - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/packages")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/packages - Should return all packages when authenticated")
    void shouldReturnAllPackages() throws Exception {
        mockMvc.perform(get("/api/v1/packages")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trackingNumber").value("PKG-001"))
                .andExpect(jsonPath("$[0].status").value("CREATED"));
    }

    // ================================================================
    // TEST 2: GET PACKAGE BY ID - Requires authentication
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/packages/{id} - Should return package by ID")
    void shouldReturnPackageById() throws Exception {
        mockMvc.perform(get("/api/v1/packages/{id}", testPackage.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testPackage.getId()))
                .andExpect(jsonPath("$.trackingNumber").value("PKG-001"))
                .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/packages/{id} - Should return 404 when package not found")
    void shouldReturn404WhenPackageNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/packages/{id}", 999L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // ================================================================
    // TEST 3: CREATE PACKAGE - Requires authentication
    // ================================================================

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /api/v1/packages - Should create package when authenticated")
    void shouldCreatePackage() throws Exception {
        PackageRequestDTO request = new PackageRequestDTO(
            "PKG-002",
            20.0,
            0.50,
            store.getId()
        );

        mockMvc.perform(post("/api/v1/packages")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
            // header agregado por parametros necesarios del controller que vienen desde el gateway como lo son los headers
            .header("X-User-Id", "user-id-123"))  
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.trackingNumber").value("PKG-002"))
            .andExpect(jsonPath("$.totalWeightKg").value(20.0))
            .andExpect(jsonPath("$.status").value("CREATED"));
    }

    @Test
    @DisplayName("POST /api/v1/packages - Should return 401 when not authenticated")
    void shouldReturn401WhenCreatingWithoutAuth() throws Exception {
        PackageRequestDTO request = new PackageRequestDTO(
            "PKG-002",
            20.0,
            0.50,
            store.getId()
        );

        mockMvc.perform(post("/api/v1/packages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // TEST 4: UPDATE PACKAGE STATUS - Requires authentication
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/v1/packages/status - Should update package status")
    void shouldUpdatePackageStatus() throws Exception {
        // First update to READY_FOR_PICKUP
        Package pkg = packageRepository.findById(testPackage.getId()).get();
        pkg.setStatus(PackageStatus.READY_FOR_PICKUP);
        packageRepository.save(pkg);

        PackageStatusUpdateRequest request = new PackageStatusUpdateRequest(
            List.of(testPackage.getId()),
            "IN_TRANSIT"
        );

        mockMvc.perform(patch("/api/v1/packages/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Verify status was updated
        Package updated = packageRepository.findById(testPackage.getId()).get();
        assertThat(updated.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/v1/packages/status - Should return 400 when invalid status")
    void shouldReturn400WhenInvalidStatus() throws Exception {
        PackageStatusUpdateRequest request = new PackageStatusUpdateRequest(
            List.of(testPackage.getId()),
            "INVALID_STATUS"
        );

        mockMvc.perform(patch("/api/v1/packages/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH /api/v1/packages/status - Should return 400 when package not ready for IN_TRANSIT")
    void shouldReturn400WhenPackageNotReady() throws Exception {
        // Package is CREATED, not READY_FOR_PICKUP
        PackageStatusUpdateRequest request = new PackageStatusUpdateRequest(
            List.of(testPackage.getId()),
            "IN_TRANSIT"
        );

        mockMvc.perform(patch("/api/v1/packages/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/packages/status - Should return 401 when not authenticated")
    void shouldReturn401WhenUpdatingStatusWithoutAuth() throws Exception {
        PackageStatusUpdateRequest request = new PackageStatusUpdateRequest(
            List.of(testPackage.getId()),
            "IN_TRANSIT"
        );

        mockMvc.perform(patch("/api/v1/packages/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // TEST 5: GET PACKAGES BY IDS (External API for ms-routes)
    // ================================================================

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/packages?ids=1,2 - Should return packages for ms-routes")
    void shouldReturnPackagesByIds() throws Exception {
        mockMvc.perform(get("/api/v1/packages")
                .param("ids", testPackage.getId().toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(testPackage.getId()))
                .andExpect(jsonPath("$[0].totalWeightKg").value(10.0))
                .andExpect(jsonPath("$[0].totalVolumeCbm").value(0.30))
                .andExpect(jsonPath("$[0].origin").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /api/v1/packages?ids=999 - Should return empty list when no packages found")
    void shouldReturnEmptyListWhenNoPackagesFound() throws Exception {
        mockMvc.perform(get("/api/v1/packages")
                .param("ids", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ================================================================
    // TEST 6: DELETE PACKAGE - Requires ADMIN role
    // ================================================================

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /api/v1/packages/{id} - Should delete package as ADMIN")
    void shouldDeletePackageAsAdmin() throws Exception {
        mockMvc.perform(delete("/api/v1/packages/{id}", testPackage.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Verify package was deleted
        mockMvc.perform(get("/api/v1/packages/{id}", testPackage.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("DELETE /api/v1/packages/{id} - Should return 403 when USER tries to delete")
    void shouldReturn403WhenUserTriesToDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/packages/{id}", testPackage.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/packages/{id} - Should return 401 when not authenticated")
    void shouldReturn401WhenDeletingWithoutAuth() throws Exception {
        mockMvc.perform(delete("/api/v1/packages/{id}", testPackage.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}