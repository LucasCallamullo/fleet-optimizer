package com.fleets.integration.controller;

// ================================================================
// IMPORTS - Jackson (JSON serialization/deserialization)
// ================================================================
import com.fasterxml.jackson.databind.ObjectMapper;
// ✅ ObjectMapper: Convierte objetos Java a JSON y viceversa
//    Se usa para enviar JSON en las peticiones POST/PUT

// ================================================================
// IMPORTS - Project DTOs and Models
// ================================================================
import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;
// ✅ Repositories: Para acceder a la base de datos en los tests

// ================================================================
// IMPORTS - JUnit 5 (Jupiter)
// ================================================================
import org.junit.jupiter.api.BeforeEach;
// ✅ @BeforeEach: Se ejecuta ANTES de CADA test
//    Útil para preparar datos frescos antes de cada test

import org.junit.jupiter.api.DisplayName;
// ✅ @DisplayName: Da un nombre legible al test
//    Se muestra en los reportes de tests

import org.junit.jupiter.api.Test;
// ✅ @Test: Marca un método como caso de prueba

// ================================================================
// IMPORTS - Spring Boot Test
// ================================================================
import org.springframework.beans.factory.annotation.Autowired;
// ✅ @Autowired: Inyección de dependencias en el test
//    Permite usar repositorios, mocks, etc.

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// ✅ @AutoConfigureMockMvc: Configura MockMvc automáticamente
//    Permite hacer peticiones HTTP sin levantar el servidor
//    ⚠️ Si no funciona: asegúrate que spring-boot-starter-test esté presente

import org.springframework.boot.test.context.SpringBootTest;
// ✅ @SpringBootTest: Carga el contexto completo de Spring
//    Similar a iniciar la aplicación, pero para pruebas

import org.springframework.http.MediaType;
// ✅ MediaType: Define el tipo de contenido (application/json)

import org.springframework.test.web.servlet.MockMvc;
// ✅ MockMvc: Simula peticiones HTTP en los tests
//    Permite hacer GET, POST, PUT, DELETE sin servidor

import org.springframework.transaction.annotation.Transactional;
// ✅ @Transactional: Cada test se ejecuta en una transacción
//    Al finalizar, hace ROLLBACK automático (no guarda cambios)

// ================================================================
// IMPORTS - Test assertions
// ================================================================
import static org.assertj.core.api.Assertions.assertThat;
// ✅ assertThat: Aserciones legibles de AssertJ
//    Ej: assertThat(result).isNotNull().hasSize(5)

import static org.hamcrest.Matchers.hasSize;
// ✅ hasSize: Matcher para verificar tamaño de listas
//    Usado con jsonPath para validar arrays

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
// ✅ Request builders: get(), post(), put(), delete()
//    Crean la petición HTTP

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// ✅ Result matchers: status(), jsonPath(), content()
//    Validan la respuesta HTTP

// ================================================================
// TEST CLASS
// ================================================================

/**
 * Integration tests for VehicleController.
 * 
 * This class tests the complete HTTP layer:
 * - Receives HTTP requests
 * - Calls the service layer
 * - Returns HTTP responses
 * - Validates JSON structure
 * 
 * It's called an "Integration Test" because it tests
 * the integration between multiple components:
 * Controller → Service → Repository → Database
 */
@SpringBootTest
// ✅ Carga el contexto completo de Spring
//    Todas las clases (Controller, Service, Repository, etc.)
//    Se inicializan como en la aplicación real
//    ⚠️ Más lento pero más completo

@AutoConfigureMockMvc
// ✅ Configura MockMvc automáticamente
//    Permite hacer peticiones HTTP sin Tomcat
//    Simula un cliente HTTP real

@Transactional
// ✅ Cada test se ejecuta en su propia transacción
//    Al finalizar: ROLLBACK automático
//    La base de datos queda limpia entre tests
//    ¡Muy importante para tests aislados!

@DisplayName("Vehicle Controller Integration Tests")
// ✅ Nombre legible para el reporte de tests
//    Se muestra en la consola y en reportes

class VehicleControllerIT {

    /**
     * ================================================================
     * DEPENDENCIES INJECTED BY SPRING
     * ================================================================
     */

    @Autowired
    private MockMvc mockMvc;
    // ✅ Simula un cliente HTTP
    //    Permite hacer: mockMvc.perform(get("/api/vehicles"))
    //    Retorna: resultado que puedes validar

    @Autowired
    private ObjectMapper objectMapper;
    // ✅ Convierte objetos Java ↔ JSON
    //    Se usa para: objectMapper.writeValueAsString(dto)
    //    Convierte el DTO en JSON para enviar en POST/PUT

    @Autowired
    private VehicleRepository vehicleRepository;
    // ✅ Repositorio real (no mock)
    //    Se usa para preparar datos y verificar resultados
    //    Ej: verificar que un vehículo se guardó correctamente

    @Autowired
    private CategoryRepository categoryRepository;
    // ✅ Repositorio real para categorías
    //    Se usa para crear categorías de prueba

    /**
     * ================================================================
     * TEST DATA - Variables de instancia
     * ================================================================
     * 
     * Estos datos se crean en setUp() y se usan en los tests.
     * Cada test recibe datos FRESCOS (nuevos).
     */

    private Category category;   // Categoría de prueba
    private Vehicle vehicle;     // Vehículo de prueba

    /**
     * ================================================================
     * SETUP - Preparar datos antes de cada test
     * ================================================================
     * 
     * @BeforeEach: Se ejecuta ANTES de CADA test.
     * 
     * Propósito:
     * 1. Limpiar la base de datos
     * 2. Crear datos de prueba
     * 3. Asegurar que cada test empieza limpio
     * 
     * Esto garantiza AISLAMIENTO entre tests.
     */
    @BeforeEach
    void setUp() {
        // Step 1: Clean database
        vehicleRepository.deleteAll();  // Elimina todos los vehículos
        categoryRepository.deleteAll(); // Elimina todas las categorías
        // ✅ Así empezamos con base de datos VACÍA

        // Step 2: Create test category
        category = new Category();
        category.setName("Sedan");
        category = categoryRepository.save(category);
        // ✅ Guarda la categoría en la base de datos (con ID generado)

        // Step 3: Create test vehicle
        vehicle = new Vehicle();
        vehicle.setLicensePlate("ABC123");
        vehicle.setYear(2023);
        vehicle.setCategory(category);
        vehicle = vehicleRepository.save(vehicle);
        // ✅ Guarda el vehículo (con ID generado y relación con Category)
    }

    /**
     * ================================================================
     * TEST 1: GET ALL VEHICLES
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles
     * Expected: Lista de todos los vehículos (solo categoryId)
     */
    @Test
    @DisplayName("GET /api/v1/vehicles - Should return all vehicles")
    void shouldReturnAllVehicles() throws Exception {
        // Step 1: Hacer la petición HTTP
        mockMvc.perform(get("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON))
                // ✅ get(): Crea una petición GET
                // ✅ contentType(): Define que esperamos JSON

        // Step 2: Validar la respuesta
                .andExpect(status().isOk())
                // ✅ status().isOk(): Verifica que sea HTTP 200 OK

                .andExpect(jsonPath("$", hasSize(1)))
                // ✅ jsonPath("$"): Accede al array completo
                // ✅ hasSize(1): Verifica que hay 1 elemento

                .andExpect(jsonPath("$[0].id").value(vehicle.getId()))
                // ✅ $[0]: Primer elemento del array
                // ✅ .id: Campo "id" del JSON
                // ✅ value(): Compara con el valor esperado

                .andExpect(jsonPath("$[0].licensePlate").value("ABC123"))
                .andExpect(jsonPath("$[0].year").value(2023))

                .andExpect(jsonPath("$[0].categoryId").value(category.getId()));
                // ✅ categoryId: Solo el ID (no el objeto completo)
    }

    /**
     * ================================================================
     * TEST 2: GET ALL VEHICLES DETAILED
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/detailed
     * Expected: Lista de vehículos con categoría completa
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/detailed - Should return all vehicles with category")
    void shouldReturnAllVehiclesDetailed() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/detailed")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(vehicle.getId()))
                .andExpect(jsonPath("$[0].licensePlate").value("ABC123"))

                // ✅ category es un OBJETO completo, no solo ID
                .andExpect(jsonPath("$[0].category.id").value(category.getId()))
                .andExpect(jsonPath("$[0].category.name").value("Sedan"));
                // ✅ category.name: Campo anidado
    }

    /**
     * ================================================================
     * TEST 3: GET VEHICLE BY ID
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/{id}
     * Expected: Vehículo con categoría completa
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return vehicle by ID")
    void shouldReturnVehicleById() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicle.getId())
                // ✅ {id}: Placeholder que se reemplaza con vehicle.getId()
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId()))
                // ✅ $: Objeto completo (no array)

                .andExpect(jsonPath("$.licensePlate").value("ABC123"))
                .andExpect(jsonPath("$.category.id").value(category.getId()));
    }

    /**
     * ================================================================
     * TEST 4: GET VEHICLE BY ID - NOT FOUND
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/999
     * Expected: HTTP 404 Not Found
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/{id} - Should return 404 when not found")
    void shouldReturn404WhenVehicleNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
                // ✅ status().isNotFound(): HTTP 404
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
        request.setLicensePlate("XYZ789");
        request.setYear(2024);
        request.setCategoryId(category.getId());

        // Step 2: Hacer la petición POST con JSON
        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                // ✅ objectMapper.writeValueAsString(): Convierte DTO a JSON
                //    Resultado: {"licensePlate":"XYZ789","year":2024,"categoryId":1}

        // Step 3: Validar respuesta
                .andExpect(status().isCreated())
                // ✅ status().isCreated(): HTTP 201 Created

                .andExpect(jsonPath("$.id").exists())
                // ✅ exists(): El campo existe (no importa el valor)

                .andExpect(jsonPath("$.licensePlate").value("XYZ789"))
                .andExpect(jsonPath("$.year").value(2024))
                .andExpect(jsonPath("$.category.id").value(category.getId()));
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
        request.setLicensePlate("ABC123"); // ← ¡Ya existe!
        request.setYear(2023);
        request.setCategoryId(category.getId());

        mockMvc.perform(post("/api/v1/vehicles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                // ✅ status().isConflict(): HTTP 409 Conflict

                .andExpect(jsonPath("$.message").value(containsString("License plate already exists")));
                // ✅ containsString(): El mensaje contiene este texto
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
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("DEF456");
        request.setYear(2025);
        request.setCategoryId(category.getId());

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicle.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId()))
                .andExpect(jsonPath("$.licensePlate").value("DEF456"))  // ← Cambió
                .andExpect(jsonPath("$.year").value(2025));            // ← Cambió
    }

    /**
     * ================================================================
     * TEST 8: DELETE VEHICLE (DELETE)
     * ================================================================
     * 
     * Endpoint: DELETE /api/v1/vehicles/{id}
     * Expected: HTTP 204 No Content
     */
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

    /**
     * ================================================================
     * TEST 9: GET VEHICLE BY LICENSE PLATE
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/license/{licensePlate}
     * Expected: Vehículo encontrado
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/license/{licensePlate} - Should return vehicle by license plate")
    void shouldReturnVehicleByLicensePlate() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/license/{licensePlate}", "ABC123")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(vehicle.getId()))
                .andExpect(jsonPath("$.licensePlate").value("ABC123"));
    }

    /**
     * ================================================================
     * TEST 10: GET VEHICLES BY CATEGORY
     * ================================================================
     * 
     * Endpoint: GET /api/v1/vehicles/category/{categoryId}
     * Expected: Lista de vehículos en esa categoría
     */
    @Test
    @DisplayName("GET /api/v1/vehicles/category/{categoryId} - Should return vehicles by category")
    void shouldReturnVehiclesByCategory() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles/category/{categoryId}", category.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(vehicle.getId()));
    }
}