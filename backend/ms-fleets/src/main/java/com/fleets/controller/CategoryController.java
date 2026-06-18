package com.fleets.controller;

import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.dto.response.CategoryDetailDTO;
import com.fleets.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Category endpoints.
 * Handles HTTP requests for category operations.
 * 
 * ================================================================
 * RESPONSIBILITIES:
 * ================================================================
 * 
 * 1. Receive HTTP requests
 * 2. Validate input (via @Valid)
 * 3. Call the appropriate service method
 * 4. Return the response (DTOs)
 * 5. Handle HTTP status codes
 * 
 * The controller is THIN - all business logic is in the service layer.
 * 
 * ================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    // ================================================================
    // GET ENDPOINTS
    // ================================================================
    
    /**
     * Retrieves all categories.
     * GET /api/v1/categories
     * 
     * @return List of CategoryResponseDTO (id + name)
     */
    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        log.info("GET /api/v1/categories - Fetching all categories");
        List<CategoryResponseDTO> result = categoryService.getAllCategories();
        log.info("GET /api/v1/categories - Returned {} categories", result.size());
        return result;
    }
    
    /**
     * Retrieves a category by its ID with full details.
     * GET /api/v1/categories/{id}
     * 
     * @param id the category ID
     * @return CategoryDetailDTO with all fields
     */
    @GetMapping("/{id}")
    public CategoryDetailDTO getCategoryById(@PathVariable Long id) {
        CategoryDetailDTO result = categoryService.getCategoryById(id);
        log.debug("Category found: {} (id: {})", result.name(), result.id());
        return result;
    }
    
    /**
     * Retrieves a category by its name.
     * GET /api/v1/categories/name/{name}
     * 
     * @param name the category name
     * @return CategoryDetailDTO if found
     * @throws AppException if not found (handled by service)
     */
    @GetMapping("/name/{name}")
    public CategoryDetailDTO getCategoryByName(@PathVariable String name) {
        log.info("GET /api/v1/categories/name/{} - Fetching category by name", name);
        CategoryDetailDTO result = categoryService.getCategoryByName(name);
        log.debug("Category found: {} (id: {})", result.name(), result.id());
        return result;
    }
    
    /**
     * Checks if a category exists by name.
     * GET /api/v1/categories/exists/{name}
     * 
     * @param name the category name
     * @return true if exists, false otherwise
     */
    @GetMapping("/exists/{name}")
    public boolean existsByName(@PathVariable String name) {
        log.info("GET /api/v1/categories/exists/{} - Checking if category exists", name);
        boolean exists = categoryService.existsByName(name);
        log.debug("Category exists: {} - {}", name, exists);
        return exists;
    }
    
    // ================================================================
    // CRUD OPERATIONS
    // ================================================================
    
    /**
     * Creates a new category.
     * POST /api/v1/categories
     * 
     * @param request the category data
     * @return CategoryDetailDTO with created category
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDetailDTO createCategory(@Valid @RequestBody CategoryRequestDTO request) {
        log.info("POST /api/v1/categories - Creating new category with name: {}", request.name());
        CategoryDetailDTO result = categoryService.createCategory(request);
        log.info("Category created successfully - id: {}, name: {}", result.id(), result.name());
        return result;
    }
    
    /**
     * Updates an existing category.
     * PUT /api/v1/categories/{id}
     * 
     * @param id the category ID
     * @param request the updated category data
     * @return CategoryDetailDTO with updated category
     */
    @PutMapping("/{id}")
    public CategoryDetailDTO updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryRequestDTO request) {
        log.info("PUT /api/v1/categories/{} - Updating category to name: {}", id, request.name());
        CategoryDetailDTO result = categoryService.updateCategory(id, request);
        log.info("Category updated successfully - id: {}, new name: {}", result.id(), result.name());
        return result;
    }
    
    /**
     * Deletes a category by ID.
     * Vehicles associated with this category will NOT be deleted.
     * DELETE /api/v1/categories/{id}
     * 
     * @param id the category ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/v1/categories/{} - Deleting category", id);
        categoryService.deleteCategory(id);
        log.info("Category deleted successfully - id: {}", id);
    }
}