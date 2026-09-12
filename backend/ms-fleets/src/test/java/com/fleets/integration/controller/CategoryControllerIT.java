package com.fleets.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.model.Category;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;
import com.fleets.utils.TestDataFactory;
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
 
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Category Controller Integration Tests")
class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();    // first childs
        categoryRepository.deleteAll();   // then categories

        category = TestDataFactory.createDefaultCategory();
        category = categoryRepository.save(category);
    }

    // ================================================================
    // TEST: GET ALL (requires authentication)
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/categories - Should return all categories")
    @WithMockUser(roles = "USER")  // ← Simula usuario autenticado
    void shouldReturnAllCategories() throws Exception {
        // this.category = { id: 1,  name: "Sedan" }

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data.[0].name").value(this.category.getName()));
    }

    // ================================================================
    // TEST: GET ALL WITHOUT AUTH (should fail)
    // ================================================================

    @Test
    @DisplayName("GET /api/v1/categories - Should return 401 when not authenticated")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isUnauthorized());  // ← 401
    }

    // ================================================================
    // TEST: CREATE (requires ADMIN role)
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/categories - Should create category as ADMIN")
    @WithMockUser(roles = "ADMIN")  // ← Simula ADMIN
    void shouldCreateCategoryAsAdmin() throws Exception {

        CategoryRequestDTO request = new CategoryRequestDTO(
            "SUV",
            "Sport Utility Vehicle",
            true
        );

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value(request.name()));
    }

    // ================================================================
    // TEST: CREATE WITH USER ROLE (should fail)
    // ================================================================

    @Test
    @DisplayName("POST /api/v1/categories - Should return 403 when USER tries to create")
    @WithMockUser(roles = "USER")  // ← USER sin permisos
    void shouldReturn403WhenUserTriesToCreate() throws Exception {
        CategoryRequestDTO request = new CategoryRequestDTO(
            "SUV",
            "Sport Utility Vehicle",
            true
        );

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());  // ← 403
    }

    // ================================================================
    // TEST: DELETE (requires ADMIN)
    // ================================================================

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should delete as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void shouldDeleteCategoryAsAdmin() throws Exception {
        // this.category = { id: 1,  name: "Sedan" }

        mockMvc.perform(delete("/api/v1/categories/{id}", this.category.getId()))
                .andExpect(status().isNoContent());

        // Verify deleted
        mockMvc.perform(get("/api/v1/categories/{id}", this.category.getId()))
                .andExpect(status().isNotFound());
    }

    // ================================================================
    // TEST: DELETE WITH USER ROLE (should fail)
    // ================================================================

    @Test
    @DisplayName("DELETE /api/v1/categories/{id} - Should return 403 when USER tries to delete")
    @WithMockUser(roles = "USER")
    void shouldReturn403WhenUserTriesToDelete() throws Exception {
        // this.category = { id: 1,  name: "Sedan" }

        mockMvc.perform(delete("/api/v1/categories/{id}", category.getId()))
                .andExpect(status().isForbidden());
    }
}