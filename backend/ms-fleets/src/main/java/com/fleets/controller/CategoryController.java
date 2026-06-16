package com.fleets.controller;

import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.model.Category;
import com.fleets.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Category endpoints.
 * Handles HTTP requests for category operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Retrieves all categories.
     * GET /api/categories
     */
    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        log.info("GET /api/categories - Fetching all categories");
        List<CategoryResponseDTO> result = categoryService.getAllCategories().stream()
            .map(CategoryResponseDTO::fromEntity)
            .collect(Collectors.toList());
        log.info("GET /api/categories - Returned {} categories", result.size());
        return result;
    }
    
    /**
     * Retrieves a category by its ID.
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public CategoryResponseDTO getCategoryById(@PathVariable Long id) {
        log.info("GET /api/categories/{} - Fetching category", id);
        Category category = categoryService.getCategoryById(id);
        log.debug("Category found: {} (id: {})", category.getName(), category.getId());
        return CategoryResponseDTO.fromEntity(category);
    }
    
    /**
     * Retrieves a category by its name.
     * GET /api/categories/name/{name}
     */
    @GetMapping("/name/{name}")
    public CategoryResponseDTO getCategoryByName(@PathVariable String name) {
        log.info("GET /api/categories/name/{} - Fetching category by name", name);
        Category category = categoryService.getCategoryByName(name);
        if (category == null) {
            log.warn("Category not found with name: {}", name);
            throw new RuntimeException("Category not found with name: " + name);
        }
        log.debug("Category found: {} (id: {})", category.getName(), category.getId());
        return CategoryResponseDTO.fromEntity(category);
    }
    
    /**
     * Creates a new category.
     * POST /api/categories
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO createCategory(@Valid @RequestBody CategoryRequestDTO request) {
        log.info("POST /api/categories - Creating new category with name: {}", request.getName());
        Category created = categoryService.createCategory(request);
        log.info("Category created successfully - id: {}, name: {}", created.getId(), created.getName());
        return CategoryResponseDTO.fromEntity(created);
    }
    
    /**
     * Updates an existing category.
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public CategoryResponseDTO updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO request) {
        log.info("PUT /api/categories/{} - Updating category to name: {}", id, request.getName());
        Category updated = categoryService.updateCategory(id, request);
        log.info("Category updated successfully - id: {}, new name: {}", updated.getId(), updated.getName());
        return CategoryResponseDTO.fromEntity(updated);
    }
    
    /**
     * Deletes a category by ID.
     * Vehicles associated with this category will NOT be deleted.
     * DELETE /api/categories/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        log.info("DELETE /api/categories/{} - Deleting category", id);
        categoryService.deleteCategory(id);
        log.info("Category deleted successfully - id: {}", id);
    }
    
    /**
     * Checks if a category exists by name.
     * GET /api/categories/exists/{name}
     */
    @GetMapping("/exists/{name}")
    public boolean existsByName(@PathVariable String name) {
        log.info("GET /api/categories/exists/{} - Checking if category exists", name);
        boolean exists = categoryService.existsByName(name);
        log.debug("Category exists: {} - {}", name, exists);
        return exists;
    }
}