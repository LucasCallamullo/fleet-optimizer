package com.fleets.service;

import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.dto.response.CategoryDetailDTO;
import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for Category business logic.
 * Defines the contract for category operations.
 * 
 * ================================================================
 * PATTERN: Two-level API
 * ================================================================
 * 
 * Why two types of methods?
 * 
 * 1. ENTITY methods (getAllCategoriesEntity, getCategoryEntityById):
 *    - Return JPA Entities
 *    - For internal use by other services
 *    - Give full access to all entity fields
 *    - Used when other services need to manipulate entities
 * 
 * 2. DTO methods (getAllCategories, getCategoryById):
 *    - Return DTOs
 *    - For REST API responses
 *    - Only expose needed fields
 *    - Decouple API from entity model
 * 
 * ================================================================
 */
public interface CategoryService {
    
    // ================================================================
    // ENTITY METHODS - For internal use
    // ================================================================
    
    /**
     * Retrieves all categories as JPA entities.
     * Used internally by other services that need to manipulate entities.
     * 
     * @return list of all Category entities
     */
    List<Category> getAllCategoriesEntity();
    
    /**
     * Finds a category entity by its unique ID.
     * 
     * @param id the category ID
     * @return the Category entity if found
     * @throws AppException if category not found (status 404)
     */
    Category getCategoryEntityById(Long id);
    
    /**
     * Finds a category entity by its name.
     * 
     * @param name the category name
     * @return the Category entity if found, null otherwise
     */
    Category getCategoryEntityByName(String name);

    /**
     * Finds a category entity by its name.
     * 
     * @param name the category name
     * @return the Category entity if found, null otherwise
     */
    Optional<Category> findCategoryEntityByName(String name);
    
    // ================================================================
    // DTO METHODS - For REST API responses
    // ================================================================
    
    /**
     * Retrieves all categories as DTOs for the REST API.
     * Maps Category entities to CategoryResponseDTO.
     * 
     * @return list of CategoryResponseDTO
     */
    List<CategoryResponseDTO> getAllCategories();
    
    /**
     * Finds a category by ID and returns it as a Detail DTO.
     * Includes all category information.
     * 
     * @param id the category ID
     * @return CategoryDetailDTO with all fields
     * @throws AppException if category not found (status 404)
     */
    CategoryDetailDTO getCategoryById(Long id);

    /**
     * Finds a category by name and returns it as a Detail DTO.
     * 
     * @param name the category name
     * @return CategoryDetailDTO with all fields
     * @throws AppException if category not found
     */
    CategoryDetailDTO getCategoryByName(String name);
    
    // ================================================================
    // CRUD OPERATIONS
    // ================================================================
    
    /**
     * Creates a new category from the request DTO.
     * 
     * @param request the DTO containing category data
     * @return the created CategoryDetailDTO
     * @throws AppException if category name already exists (status 409)
     */
    CategoryDetailDTO createCategory(CategoryRequestDTO request);
    
    /**
     * Updates an existing category.
     * 
     * @param id the ID of the category to update
     * @param request the DTO containing updated category data
     * @return the updated CategoryDetailDTO
     * @throws AppException if category not found (status 404)
     * @throws AppException if new name already exists on another category (status 409)
     */
    CategoryDetailDTO updateCategory(Long id, CategoryRequestDTO request);
    
    /**
     * Deletes a category by its ID.
     * 
     * @param id the ID of the category to delete
     * @throws AppException if category not found (status 404)
     * @throws AppException if category has associated vehicles (status 409)
     */
    void deleteCategory(Long id);
    
    // ================================================================
    // UTILITY METHODS
    // ================================================================
    
    /**
     * Checks if a category exists by name.
     * 
     * @param name the category name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}