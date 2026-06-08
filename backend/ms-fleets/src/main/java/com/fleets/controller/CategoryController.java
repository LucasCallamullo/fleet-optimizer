package com.fleets.controller;

import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.model.Category;
import com.fleets.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Category endpoints.
 * Handles HTTP requests for category operations.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryService categoryService;
    
    /**
     * Retrieves all categories.
     * GET /api/categories
     */
    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.getAllCategories().stream()
            .map(CategoryResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a category by its ID.
     * GET /api/categories/{id}
     */
    @GetMapping("/{id}")
    public CategoryResponseDTO getCategoryById(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        return CategoryResponseDTO.fromEntity(category);
    }
    
    /**
     * Retrieves a category by its name.
     * GET /api/categories/name/{name}
     */
    @GetMapping("/name/{name}")
    public CategoryResponseDTO getCategoryByName(@PathVariable String name) {
        Category category = categoryService.getCategoryByName(name);
        if (category == null) {
            throw new RuntimeException("Category not found with name: " + name);
        }
        return CategoryResponseDTO.fromEntity(category);
    }
    
    /**
     * Creates a new category.
     * POST /api/categories
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO createCategory(@RequestBody Category category) {
        Category created = categoryService.createCategory(category);
        return CategoryResponseDTO.fromEntity(created);
    }
    
    /**
     * Updates an existing category.
     * PUT /api/categories/{id}
     */
    @PutMapping("/{id}")
    public CategoryResponseDTO updateCategory(@PathVariable Long id, @RequestBody Category category) {
        Category updated = categoryService.updateCategory(id, category);
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
        categoryService.deleteCategory(id);
    }
    
    /**
     * Checks if a category exists by name.
     * GET /api/categories/exists/{name}
     */
    @GetMapping("/exists/{name}")
    public boolean existsByName(@PathVariable String name) {
        return categoryService.existsByName(name);
    }
}