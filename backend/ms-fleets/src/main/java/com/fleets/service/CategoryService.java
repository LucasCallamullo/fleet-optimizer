package com.fleets.service;

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
     * @throws RuntimeException if category not found
     */
    Category getCategoryById(Long id);
    
    /**
     * Finds a category by its name.
     * @param name the category name (e.g., "Truck", "Car")
     * @return the category if found, null otherwise
     */
    Category getCategoryByName(String name);
    
    /**
     * Creates a new category.
     * @param category the category to create
     * @return the created category with generated ID
     * @throws RuntimeException if category name already exists
     */
    Category createCategory(Category category);
    
    /**
     * Updates an existing category.
     * @param id the ID of the category to update
     * @param categoryDetails the updated category data
     * @return the updated category
     * @throws RuntimeException if category not found or name already exists
     */
    Category updateCategory(Long id, Category categoryDetails);
    
    /**
     * Deletes a category by its ID.
     * Vehicles belonging to this category will NOT be deleted.
     * Their category_id will be set to NULL.
     * @param id the ID of the category to delete
     * @throws RuntimeException if category not found or has associated vehicles
     */
    void deleteCategory(Long id);
    
    /**
     * Checks if a category exists by name.
     * @param name the category name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}