package com.fleets.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
// import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

// IMPORTS - Project DTOs and Models
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
 * 
 * Each test runs in an isolated transaction with automatic rollback.
 * Test data is created fresh before each test method.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
// @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
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
    // TEST 1: GET ALL VEHICLES (Basic)
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles
     * 
     * Verifies that the endpoint returns a list of all vehicles.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns JSON array with 5 vehicles
     * - Each vehicle contains: id, licensePlate, year, categoryId
     * - No nested category object (flat structure)
     */
    @Test
    @DisplayName("GET /api/v1/vehicles - Should return all vehicles")
    void shouldReturnAllVehicles() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                
                // Validate first vehicle (ABC123)
                .andExpect(jsonPath("$[0].licensePlate").value("ABC123"))
                .andExpect(jsonPath("$[0].year").value(2023))
                .andExpect(jsonPath("$[0].categoryId").value(sedanCategory.getId()))
                
                // Validate second vehicle (XYZ789)
                .andExpect(jsonPath("$[1].licensePlate").value("XYZ789"))
                .andExpect(jsonPath("$[1].year").value(2024))
                .andExpect(jsonPath("$[1].categoryId").value(suvCategory.getId()))
                
                // Validate IDs exist (auto-generated)
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].id").isNumber());
    }

    // ================================================================
    // TEST 2: GET ALL VEHICLES (Detailed)
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles/detailed
     * 
     * Verifies that the endpoint returns a list of all vehicles
     * with full category objects nested inside.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns JSON array with 5 vehicles
     * - Each vehicle contains: id, licensePlate, year, category (object)
     * - Category object includes: id, name, active
     * - Vehicles without category have null category
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/detailed - Should return all vehicles with categories")
    void shouldReturnAllVehiclesWithCategory() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/detailed")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                
                // Validate first vehicle (ABC123) has category
                .andExpect(jsonPath("$[0].licensePlate").value("ABC123"))
                .andExpect(jsonPath("$[0].category.id").value(sedanCategory.getId()))
                .andExpect(jsonPath("$[0].category.name").value("Sedan"))
                
                // Validate fifth vehicle (JKL012) has no category
                .andExpect(jsonPath("$[4].licensePlate").value("JKL012"))
                .andExpect(jsonPath("$[4].category").doesNotExist())
                
                // Validate IDs exist
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[4].id").isNumber());
    }

    // ================================================================
    // TEST 3: GET VEHICLE BY ID (With Category)
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles/{id}
     * 
     * Verifies that the endpoint returns a specific vehicle by ID
     * with its full category object.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns vehicle object with nested category
     * - Category contains id and name
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID with Category")
    void shouldReturnVehicleById() throws Exception {
        Long vehicleId = seedVehicles.get(1).getId();
        Category category = seedVehicles.get(1).getCategory();

        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("XYZ789"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.category.id").value(category.getId()))
                .andExpect(jsonPath("$.category.name").value(category.getName()));
    }

    // ================================================================
    // TEST 4: GET VEHICLE BY ID (Without Category)
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles/{id}
     * 
     * Verifies that the endpoint returns a vehicle without category
     * when the vehicle has no associated category.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns vehicle object without category field
     * - category field does not exist (null is omitted from JSON)
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID without Category")
    void shouldReturnVehicleByIdAndCategoryNull() throws Exception {
        Long vehicleId = seedVehicles.get(4).getId();

        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("JKL012"))
                .andExpect(jsonPath("$.year").value(2023))
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    // ================================================================
    // TEST 5: GET VEHICLE BY ID (Not Found)
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles/{id} - Error case
     * 
     * Verifies that the endpoint returns 404 Not Found when
     * a vehicle with the given ID does not exist.
     * 
     * Expected Behavior:
     * - HTTP 404 Not Found
     * - ErrorResponse with status 404
     * - ErrorResponse with path /api/v1/vehicles/999
     * - ErrorResponse with timestamp
     * - ErrorResponse with error message
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return 404 Not Found if vehicle doesn't exist")
    void shouldReturnErrorVehicleById() throws Exception { 
        Long nonExistentId = 999L; 

        mockMvc.perform(get("/api/v1/vehicles/{id}", nonExistentId)
            .contentType(MediaType.APPLICATION_JSON))
            
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/v1/vehicles/999"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.error").isString());
    }

    // ================================================================
    // TEST 6: CREATE VEHICLE (With Category)
    // ================================================================
    
    /**
     * Test POST /api/v1/vehicles
     * 
     * Verifies that the endpoint creates a new vehicle with category.
     * 
     * Expected Behavior:
     * - HTTP 201 Created
     * - Returns created vehicle with auto-generated ID
     * - Vehicle has correct licensePlate, year, and category
     * - Category object is included in response
     */
    @Test
    @DisplayName("POST /api/v1/vehicles - Should create vehicle with category")
    void shouldCreateVehicle() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("FCD333");
        request.setYear(2024);
        request.setCategoryId(sedanCategory.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.licensePlate").value("FCD333"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.category.id").value(sedanCategory.getId()))
                .andExpect(jsonPath("$.category.name").value("Sedan"));
    }

    // ================================================================
    // TEST 7: CREATE VEHICLE (Without Category)
    // ================================================================
    
    /**
     * Test POST /api/v1/vehicles
     * 
     * Verifies that the endpoint creates a new vehicle without category.
     * 
     * Expected Behavior:
     * - HTTP 201 Created
     * - Returns created vehicle with auto-generated ID
     * - Vehicle has correct licensePlate and year
     * - No category field in response (null is omitted)
     */
    @Test
    @DisplayName("POST /api/v1/vehicles - Should create vehicle without category")
    void shouldCreateVehicleWithoutCategory() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("FCD333");
        request.setYear(2024);

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.licensePlate").value("FCD333"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    // ================================================================
    // TEST 8: CREATE VEHICLE (Duplicate License Plate)
    // ================================================================
    
    /**
     * Test POST /api/v1/vehicles - Error case
     * 
     * Verifies that the endpoint returns 409 Conflict when trying
     * to create a vehicle with a license plate that already exists.
     * 
     * Expected Behavior:
     * - HTTP 409 Conflict
     * - ErrorResponse with status 409
     * - ErrorResponse with appropriate error message
     */
    @Test
    @DisplayName("POST /api/v1/vehicles - Should return 409 when duplicate license plate")
    void shouldReturn409WhenDuplicateLicensePlate() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ABC123"); // Already exists in seed data
        request.setYear(2023);
        request.setCategoryId(sedanCategory.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"))
                .andExpect(jsonPath("$.error").isString());
    }

    // ================================================================
    // TEST 9: UPDATE VEHICLE (All Fields)
    // ================================================================
    
    /**
     * Test PUT /api/v1/vehicles/{id}
     * 
     * Verifies that the endpoint updates all vehicle fields.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns updated vehicle
     * - licensePlate and year are updated
     * - category remains the same (unchanged)
     */
    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should update vehicle fully")
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
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.licensePlate").value("ASD666"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.category.id").value(category.getId()))
                .andExpect(jsonPath("$.category.name").value(category.getName()));
    }

    // ================================================================
    // TEST 10: UPDATE VEHICLE (Remove Category)
    // ================================================================
    
    /**
     * Test PUT /api/v1/vehicles/{id}
     * 
     * Verifies that the endpoint removes the category from a vehicle.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns updated vehicle
     * - licensePlate and year are updated
     * - category is set to null (field does not exist in JSON)
     */
    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should update vehicle and remove category")
    void shouldUpdateVehicleCategoryNull() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();

        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ASD666");
        request.setYear(2025);
        request.setCategoryId(null); // Remove category

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.licensePlate").value("ASD666"))
                .andExpect(jsonPath("$.year").value(2025))
                .andExpect(jsonPath("$.category").doesNotExist());
    }

    // ================================================================
    // TEST 11: DELETE VEHICLE
    // ================================================================
    
    /**
     * Test DELETE /api/v1/vehicles/{id}
     * 
     * Verifies that the endpoint deletes a vehicle.
     * 
     * Expected Behavior:
     * - HTTP 204 No Content
     * - Vehicle is removed from the database
     * - Subsequent findById returns empty Optional
     */
    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Should delete vehicle")
    void shouldDeleteVehicle() throws Exception {
        Long vehicleId = seedVehicles.get(3).getId();

        // Perform DELETE request
        mockMvc.perform(delete("/api/v1/vehicles/{id}", vehicleId))
                .andExpect(status().isNoContent());

        // Verify vehicle was removed from database
        assertThat(vehicleRepository.findById(vehicleId).isEmpty()).isTrue();
    }

    // ================================================================
    // TEST 12: GET VEHICLE BY LICENSE PLATE
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles/license/{licensePlate}
     * 
     * Verifies that the endpoint finds a vehicle by its license plate.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns the vehicle with matching license plate
     * - Vehicle contains id, licensePlate, and other fields
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/license/{licensePlate} - Should return vehicle by license plate")
    void shouldReturnVehicleByLicensePlate() throws Exception {
        Long vehicleId = seedVehicles.get(1).getId();
        String licensePlate = seedVehicles.get(1).getLicensePlate();

        mockMvc.perform(get("/api/v1/vehicles/license/{licensePlate}", licensePlate)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.licensePlate").value(licensePlate));
    }

    // ================================================================
    // TEST 13: GET VEHICLES BY CATEGORY
    // ================================================================
    
    /**
     * Test GET /api/v1/vehicles/category/{categoryId}
     * 
     * Verifies that the endpoint returns all vehicles belonging
     * to a specific category.
     * 
     * Expected Behavior:
     * - HTTP 200 OK
     * - Returns JSON array with vehicles in the category
     * - All returned vehicles have the correct categoryId
     * - Number of vehicles matches expected count
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/category/{categoryId} - Should return vehicles by category")
    void shouldReturnVehiclesByCategory() throws Exception {
        Long vehicleId = seedVehicles.get(0).getId();
        Long categoryId = seedVehicles.get(0).getCategory().getId();

        mockMvc.perform(get("/api/v1/vehicles/category/{categoryId}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2))) // Exactly 2 vehicles in Sedan category
                .andExpect(jsonPath("$[0].id").value(vehicleId))
                .andExpect(jsonPath("$[0].category.id").value(categoryId))
                .andExpect(jsonPath("$[1].category.id").value(categoryId));
    }
}