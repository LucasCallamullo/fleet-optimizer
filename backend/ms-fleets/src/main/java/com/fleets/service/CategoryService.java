package com.fleets.service;

import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import java.util.List;

/**
 * Service interface for Category business logic.
 * Defines the contract for category operations.
 */
public interface CategoryService {
    
    /**
     * Retrieves all categories from the system.
     * @return list of all categories
     */
    List<Category> getAllCategories();
    
    /**
     * Finds a category by its unique ID.
     * @param id the category ID
     * @return the category if found
     * @throws AppException if category not found (status 404)
     */
    Category getCategoryById(Long id);
    
    /**
     * Finds a category by its name.
     * @param name the category name (e.g., "Truck", "Car")
     * @return the category if found, null otherwise
     */
    Category getCategoryByName(String name);
    
    /**
     * Creates a new category from the request DTO.
     * @param request the DTO containing category data
     * @return the created category with generated ID
     * @throws AppException if category name already exists (status 409)
     */
    Category createCategory(CategoryRequestDTO request);
    
    /**
     * Updates an existing category from the request DTO.
     * @param id the ID of the category to update
     * @param request the DTO containing updated category data
     * @return the updated category
     * @throws AppException if category not found (status 404)
     * @throws AppException if new name already exists on another category (status 409)
     */
    Category updateCategory(Long id, CategoryRequestDTO request);
    
    /**
     * Deletes a category by its ID.
     * Vehicles belonging to this category will NOT be deleted.
     * Their category_id will be set to NULL.
     * @param id the ID of the category to delete
     * @throws AppException if category not found (status 404)
     * @throws AppException if category has associated vehicles (status 409)
     */
    void deleteCategory(Long id);
    
    /**
     * Checks if a category exists by name.
     * @param name the category name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}