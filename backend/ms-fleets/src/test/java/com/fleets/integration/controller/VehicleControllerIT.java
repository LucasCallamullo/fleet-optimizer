package com.fleets.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

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

import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;
import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.utils.TestDataFactory;

/**
 * Integration tests for VehicleController.
 * 
 * This test class validates the complete HTTP layer including:
 * - Request/Response mapping
 * - JSON serialization/deserialization
 * - Business logic validation
 * - Database operations
 * - Error handling
 * - Security (authentication and authorization)
 * 
 * Each test runs in an isolated transaction with automatic rollback.
 * Test data is created fresh before each test method.
 * 
 * Security:
 * - Tests use @WithMockUser to simulate authenticated users
 * - GET endpoints: USER role (authenticated)
 * - POST/PUT/DELETE endpoints: ADMIN role (for authorization testing)
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Vehicle Controller Integration Tests")
class VehicleControllerIT {

    // ================================================================
    // DEPENDENCIES
    // ================================================================
    
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // Test data instances
    private List<Vehicle> seedVehicles;
    private Category sedanCategory;
    private Category suvCategory;
    private Category truckCategory;

    // ================================================================
    // SETUP - Executes before each test
    // ================================================================
    
    @BeforeEach
    void setUp() {
        // Clean database
        vehicleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();

        // Create categories using TestDataFactory
        this.sedanCategory = TestDataFactory.createDefaultCategory();
        this.suvCategory = TestDataFactory.createCategorySUV();
        this.truckCategory = TestDataFactory.createCategoryTruck();

        // Persist categories
        this.sedanCategory = categoryRepository.save(sedanCategory);
        this.suvCategory = categoryRepository.save(suvCategory);
        this.truckCategory = categoryRepository.save(truckCategory);

        // Create vehicles with persisted categories
        List<Vehicle> vehicles = TestDataFactory.createSeedVehicles(List.of(
            this.sedanCategory, this.suvCategory, this.truckCategory
        ));

        this.seedVehicles = vehicleRepository.saveAll(vehicles);
    }

    // ================================================================
    // TEST 1: GET ALL VEHICLES (Basic) - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles - Should return all vehicles")
    @WithMockUser(roles = "USER")  // ← Simulates an authenticated user with the USER role
    void shouldReturnAllVehicles() throws Exception {
        // vehicle[0] = { "ABC123", 2023, sedanCategory }
        // this.seedVehicles[0]
        var vehicle = this.seedVehicles.get(0);

        mockMvc.perform(get("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[0].licensePlate").value(vehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data[0].year").value(vehicle.getYear()))
                .andExpect(jsonPath("$.data[0].categoryId").value(vehicle.getCategory().getId()))
                .andExpect(jsonPath("$.data[0].id").isNumber());
    }

    // ================================================================
    // TEST 2: GET ALL VEHICLES WITHOUT AUTH - Should fail
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        // WITHOUT @WithMockUser - should fail with 401
        mockMvc.perform(get("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    // ================================================================
    // TEST 3: GET ALL VEHICLES (Detailed) - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles/detailed - Should return all vehicles with categories")
    @WithMockUser(roles = "USER")
    void shouldReturnAllVehiclesWithCategory() throws Exception {
        // vehicle[0] = { "ABC123", 2023, sedanCategory }
        // this.seedVehicles[0]
        var vehicle = this.seedVehicles.get(0);
        var vehicleFour = this.seedVehicles.get(4);

        mockMvc.perform(get("/api/v1/vehicles/detailed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(5)))
                .andExpect(jsonPath("$.data[0].licensePlate").value(vehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data[0].category.id").value(vehicle.getCategory().getId()))
                .andExpect(jsonPath("$.data[0].category.name").value(vehicle.getCategory().getName()))
                .andExpect(jsonPath("$.data[4].licensePlate").value(vehicleFour.getLicensePlate()))
                .andExpect(jsonPath("$.data[4].category").doesNotExist());
    }

    // ================================================================
    // TEST 4: GET VEHICLE BY ID (With Category) - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID with Category")
    @WithMockUser(roles = "USER")
    void shouldReturnVehicleById() throws Exception {
        var vehicle = seedVehicles.get(1);
        Long vehicleId = vehicle.getId();
        Category category = vehicle.getCategory();

        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.licensePlate").value(vehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data.year").value(vehicle.getYear()))
                .andExpect(jsonPath("$.data.category.id").value(category.getId()))
                .andExpect(jsonPath("$.data.category.name").value(category.getName()));
    }

    // ================================================================
    // TEST 5: GET VEHICLE BY ID (Without Category) - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID without Category")
    @WithMockUser(roles = "USER")
    void shouldReturnVehicleByIdAndCategoryNull() throws Exception {
        var vehicle = this.seedVehicles.get(4);
        Long vehicleId = vehicle.getId();

        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.licensePlate").value(vehicle.getLicensePlate()))
                .andExpect(jsonPath("$.data.year").value(vehicle.getYear()))
                .andExpect(jsonPath("$.data.category").doesNotExist());
    }

    // ================================================================
    // TEST 6: GET VEHICLE BY ID (Not Found) - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return 404 Not Found if vehicle doesn't exist")
    @WithMockUser(roles = "USER")
    void shouldReturnErrorVehicleById() throws Exception { 
        Long nonExistentId = 999L; 

        mockMvc.perform(get("/api/v1/vehicles/{id}", nonExistentId)
            .contentType(MediaType.APPLICATION_JSON))
            
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/v1/vehicles/999"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.detail").isString());
    }

    // ================================================================
    // TEST 7: CREATE VEHICLE (With Category) - Requires ADMIN role
    // ================================================================
    
    @Test
    @DisplayName("POST /api/v1/vehicles - Should create vehicle with category as ADMIN")
    @WithMockUser(roles = "ADMIN")  // ← Simula ADMIN (requerido para crear)
    void shouldCreateVehicle() throws Exception {

        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("FCD333");
        request.setYear(2024);
        request.setCategoryId(sedanCategory.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isCreated())    // 404 ?
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.licensePlate").value("FCD333"))
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.category.id").value(sedanCategory.getId()))
                .andExpect(jsonPath("$.data.category.name").value(sedanCategory.getName())); 
    }

    // ================================================================
    // TEST 8: CREATE VEHICLE WITH USER ROLE - Should fail (403)
    // ================================================================
    
    @Test
    @DisplayName("POST /api/v1/vehicles - Should return 403 when USER tries to create")
    @WithMockUser(roles = "USER")  // ← USER sin permisos de ADMIN
    void shouldReturn403WhenUserTriesToCreate() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("FCD333");
        request.setYear(2024);
        request.setCategoryId(sedanCategory.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isForbidden());  // ← 403 Forbidden
    }

    // ================================================================
    // TEST 9: CREATE VEHICLE (Without Category) - Requires ADMIN role
    // ================================================================
    
    @Test
    @DisplayName("POST /api/v1/vehicles - Should create vehicle without category as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldCreateVehicleWithoutCategory() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("FCD333");
        request.setYear(2024);

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.licensePlate").value("FCD333"))
                .andExpect(jsonPath("$.data.year").value(2024))
                .andExpect(jsonPath("$.data.category").doesNotExist());
    }

    // ================================================================
    // TEST 10: CREATE VEHICLE (Duplicate) - Requires ADMIN role
    // ================================================================
    
    @Test
    @DisplayName("POST /api/v1/vehicles - Should return 409 when duplicate license plate")
    @WithMockUser(roles = "ADMIN")
    void shouldReturn409WhenDuplicateLicensePlate() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ABC123");
        request.setYear(2023);
        request.setCategoryId(sedanCategory.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.detail").isString());
    }

    // ================================================================
    // TEST 11: UPDATE VEHICLE - Requires ADMIN role
    // ================================================================
    
    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should update vehicle fully as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateVehicle() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();
        Category category = seedVehicles.get(3).getCategory();

        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ASD666");
        request.setYear(2025);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(vehicleId))
                .andExpect(jsonPath("$.data.licensePlate").value("ASD666"))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.category.id").value(category.getId()));
    }

    // ================================================================
    // TEST 12: UPDATE VEHICLE WITH USER ROLE - Should fail (403)
    // ================================================================
    
    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should return 403 when USER tries to update")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToUpdate() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();
        Category category = seedVehicles.get(3).getCategory();

        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ASD666");
        request.setYear(2025);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isForbidden());
    }

    // ================================================================
    // TEST 13: UPDATE VEHICLE (Remove Category) - Requires ADMIN role
    // ================================================================
    
    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should update vehicle and remove category as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldUpdateVehicleCategoryNull() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();

        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ASD666");
        request.setYear(2025);
        request.setCategoryId(null);

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(vehicleId))
                .andExpect(jsonPath("$.data.licensePlate").value("ASD666"))
                .andExpect(jsonPath("$.data.year").value(2025))
                .andExpect(jsonPath("$.data.category").doesNotExist());
    }

    // ================================================================
    // TEST 14: DELETE VEHICLE - Requires ADMIN role
    // ================================================================
    
    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Should delete vehicle as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteVehicle() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();

        mockMvc.perform(delete("/api/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isNoContent());

        assertThat(vehicleRepository.findById(vehicleId).isEmpty()).isTrue();
    }

    // ================================================================
    // TEST 15: DELETE VEHICLE WITH USER ROLE - Should fail (403)
    // ================================================================
    
    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Should return 403 when USER tries to delete")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToDelete() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();

        mockMvc.perform(delete("/api/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isForbidden());
    }

    // ================================================================
    // TEST 16: GET VEHICLE BY LICENSE PLATE - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles/license/{licensePlate} - Should return vehicle by license plate")
    @WithMockUser(roles = "USER")
    void shouldReturnVehicleByLicensePlate() throws Exception {
        Long vehicleId = seedVehicles.get(1).getId();
        String licensePlate = seedVehicles.get(1).getLicensePlate();

        mockMvc.perform(get("/api/v1/vehicles/license/{licensePlate}", licensePlate)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(vehicleId))
                .andExpect(jsonPath("$.data.licensePlate").value(licensePlate));
    }

    // ================================================================
    // TEST 17: GET VEHICLES BY CATEGORY - Requires USER role
    // ================================================================
    
    @Test
    @DisplayName("GET /api/v1/vehicles/category/{categoryId} - Should return vehicles by category")
    @WithMockUser(roles = "USER")
    void shouldReturnVehiclesByCategory() throws Exception {
        Long vehicleId = seedVehicles.get(0).getId();
        Long categoryId = seedVehicles.get(0).getCategory().getId();

        mockMvc.perform(get("/api/v1/vehicles/category/{categoryId}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].id").value(vehicleId))
                .andExpect(jsonPath("$.data[0].category.id").value(categoryId))
                .andExpect(jsonPath("$.data[1].category.id").value(categoryId));
    }
}