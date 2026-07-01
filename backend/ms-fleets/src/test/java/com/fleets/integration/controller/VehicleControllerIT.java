package com.fleets.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
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

// TEST CLASS
@SpringBootTest
@AutoConfigureMockMvc
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
        // Step 1: Clean database
        vehicleRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();

        // Step 2: Create categories using TestDataFactory
        sedanCategory = TestDataFactory.createDefaultCategory();  // name="Sedan"
        suvCategory = TestDataFactory.createCategorySUV();        // name="SUV"
        truckCategory = TestDataFactory.createCategoryTruck();    // name="Truck"

        // Step 3: Persist categories
        sedanCategory = categoryRepository.save(sedanCategory);
        suvCategory = categoryRepository.save(suvCategory);
        truckCategory = categoryRepository.save(truckCategory);

        // Step 4: Create vehicles with persisted categories
        // NO asignar IDs - serán generados por la base de datos
        Vehicle vehicle1 = TestDataFactory.createVehicle("ABC123", 2023, sedanCategory);
        Vehicle vehicle2 = TestDataFactory.createVehicle("XYZ789", 2024, suvCategory);
        Vehicle vehicle3 = TestDataFactory.createVehicle("DEF456", 2025, truckCategory);
        Vehicle vehicle4 = TestDataFactory.createVehicle("GHI789", 2022, sedanCategory);
        Vehicle vehicle5 = TestDataFactory.createVehicle("JKL012", 2023, null);

        // Step 5: Persist vehicles
        seedVehicles = vehicleRepository.saveAll(List.of(
            vehicle1, vehicle2, vehicle3, vehicle4, vehicle5
        ));
    }

    // ================================================================
    // TEST 1: GET ALL VEHICLES
    // ================================================================
    
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
                
                // Validate IDs exist (don't check exact values)
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[1].id").isNumber());
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/detailed - Should return all vehicles with categories")
    void shouldReturnAllVehiclesWithCategory() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/detailed")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(5)))
                
                // Validate first vehicle (ABC123)
                .andExpect(jsonPath("$[0].licensePlate").value("ABC123"))
                .andExpect(jsonPath("$[0].category.id").value(sedanCategory.getId()))
                
                // Validate second vehicle (XYZ789)
                .andExpect(jsonPath("$[4].licensePlate").value("JKL012"))
                // SI SABES QUE EL ELEMENTO [4] NO TIENE CATEGORIA (Es Null):
                // Probás su nulidad explícitamente con cualquiera de estas dos opciones:
                .andExpect(jsonPath("$[4].category").value(org.hamcrest.Matchers.nullValue()))
                // o: .andExpect(jsonPath("$[4].category").doesNotExist());
                
                // Validate IDs exist (don't check exact values)
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[4].id").isNumber());
    }

    // ================================================================
    // TEST 2: GET VEHICLE BY ID
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID")
    void shouldReturnVehicleById() throws Exception {
        // Get the ID from the persisted vehicle
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

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID")
    void shouldReturnVehicleByIdAndCategoryNull() throws Exception {
        // Get the ID from the persisted vehicle
        Long vehicleId = seedVehicles.get(4).getId();
        // Category category = seedVehicles.get(4).getCategory();

        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return 404 Not Found if vehicle doesn't exist")
    void shouldReturnErrorVehicleById() throws Exception { 
        // An ID that we know perfectly well does not exist in the seed 
        Long nonExistentId = 999L; 

        mockMvc.perform(get("/api/v1/vehicles/{id}", nonExistentId)
            .contentType(MediaType.APPLICATION_JSON))

            // 1. Validate that the HTTP status is correct (404)
            .andExpect(status().isNotFound())

            // 2. Validate the exact structure of your ErrorResponse record
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.path").value("/api/v1/vehicles/999"))
            .andExpect(jsonPath("$.timestamp").exists())

            // 3. Validate that the message contains useful information
            .andExpect(jsonPath("$.error").isString());
    }

    
    /**
     * ================================================================
     * TEST 5: CREATE VEHICLE (POST)
     * ================================================================
     * 
     * Endpoint: POST /api/v1/vehicles
     * Expected: HTTP 201 Created con el vehículo creado
    */
    @Test
    @DisplayName("POST /api/v1/vehicles - Should create vehicle")
    void shouldCreateVehicle() throws Exception {
        // Step 1: Preparar el request DTO
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("FCD333");
        request.setYear(2024);
        request.setCategoryId(sedanCategory.getId());

        // Step 2: Hacer la petición POST con JSON
        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // objectMapper.writeValueAsString(): Convierte DTO a JSON
                //    Resultado: {"licensePlate":"XYZ789","year":2024,"category": {"id": 1, name:"Sedan"}}

                // Step 3: Validar respuesta
                .andExpect(status().isCreated())
                // status().isCreated(): HTTP 201 Created

                .andExpect(jsonPath("$.id").exists())
                // exists(): El campo existe (no importa el valor)

                .andExpect(jsonPath("$.licensePlate").value("FCD333"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.category.id").value(sedanCategory.getId()));
    }

    /**
     * ================================================================
     * TEST 6: CREATE VEHICLE - DUPLICATE
     * ================================================================
     * 
     * Endpoint: POST /api/v1/vehicles
     * Expected: HTTP 409 Conflict (duplicate license plate)
    */
    @Test
    @DisplayName("POST /api/v1/vehicles - Should return 409 when duplicate license plate")
    void shouldReturn409WhenDuplicateLicensePlate() throws Exception {
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ABC123"); // ← exist
        request.setYear(2023);
        request.setCategoryId(sedanCategory.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())    // HTTP 409 Conflict

                // 2. Validate the exact structure of your ErrorResponse record
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.path").value("/api/v1/vehicles"));
    }
  
    /**
     * ================================================================
     * TEST 7: UPDATE VEHICLE (PUT)
     * ================================================================
     * 
     * Endpoint: PUT /api/v1/vehicles/{id}
     * Expected: HTTP 200 OK con vehículo actualizado
     */
    @Test
    @DisplayName("PUT /api/v1/vehicles/{id} - Should update vehicle")
    void shouldUpdateVehicle() throws Exception {

        Long vehicleId = this.seedVehicles.get(3).getId();
        Category category = this.seedVehicles.get(3).getCategory();

        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ASD666");
        request.setYear(2025);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())

                // test resposne 
                .andExpect(jsonPath("$.id").value(vehicleId))
                .andExpect(jsonPath("$.licensePlate").value("ASD666"))  // Change
                .andExpect(jsonPath("$.year").value(2025))            // Change
                .andExpect(jsonPath("$.category.id").value(category.getId()));        // Dont Change
    } 

    /**
     * ================================================================
     * TEST 8: DELETE VEHICLE (DELETE)
     * ================================================================
     * 
     * Endpoint: DELETE /api/v1/vehicles/{id}
     * Expected: HTTP 204 No Content
    
    @Test
    @DisplayName("DELETE /api/v1/vehicles/{id} - Should delete vehicle")
    void shouldDeleteVehicle() throws Exception {
        // Step 1: Hacer DELETE
        mockMvc.perform(delete("/api/v1/vehicles/{id}", vehicle.getId()))
                .andExpect(status().isNoContent());
                // ✅ status().isNoContent(): HTTP 204 No Content

        // Step 2: Verificar que se eliminó de la BD
        assertThat(vehicleRepository.findById(vehicle.getId())).isEmpty();
        // ✅ assertThat(...).isEmpty(): Verifica que Optional esté vacío
    }
 */
    /**
     * ================================================================
     * TEST 9: GET VEHICLE BY LICENSE PLATE
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/license/{licensePlate}
     * Expected: Vehículo encontrado
   
    @Test
    @DisplayName("GET /api/v1/vehicles/license/{licensePlate} - Should return vehicle by license plate")
    void shouldReturnVehicleByLicensePlate() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/license/{licensePlate}", "ABC123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId()))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"));
    }
  */
    /**
     * ================================================================
     * TEST 10: GET VEHICLES BY CATEGORY
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/category/{categoryId}
     * Expected: Lista de vehículos en esa categoría
     
    @Test
    @DisplayName("GET /api/v1/vehicles/category/{categoryId} - Should return vehicles by category")
    void shouldReturnVehiclesByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/category/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(vehicle.getId()));
    }*/
}