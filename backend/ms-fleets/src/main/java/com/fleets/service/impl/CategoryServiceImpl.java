package com.fleets.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

import com.fleets.exception.AppException;
import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.dto.response.CategoryDetailDTO;
import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.model.Category;
import com.fleets.service.CategoryService;
import com.fleets.repository.CategoryRepository;
import com.fleets.mapper.CategoryMapper;

/**
 * ================================================================
 * CATEGORY SERVICE IMPLEMENTATION
 * ================================================================
 * 
 * JPA-based implementation of CategoryService.
 * Uses Spring Data JPA for database operations.
 * 
 * PATTERNS USED:
 * 1. Repository Pattern - Data access abstraction
 * 2. DTO Pattern - Data transfer between layers
 * 3. Mapper Pattern - Entity ↔ DTO conversion
 * 4. Transactional - Database consistency
 * 5. Exception Handling - Business rule validation
 * 
 * ================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // Most methods are read-only by default
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper mapper;
    
    // ================================================================
    // ENTITY METHODS - For internal use (other services)
    // ================================================================
    
    /**
     * {@inheritDoc}
     * 
     * @return List of Category entities
     *         Used internally by other services that need Category entities
     */
    @Override
    public List<Category> getAllCategoriesEntity() {
        log.debug("Fetching all categories from database");
        List<Category> categories = categoryRepository.findAll();
        log.debug("Found {} categories in database", categories.size());
        return categories;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param id - The category ID
     * @return Category entity
     * @throws AppException if not found
     */
    @Override
    public Category getCategoryEntityById(Long id) {
        log.debug("Fetching category entity by id: {}", id);
        return categoryRepository.findById(id)
            .orElseThrow(() -> new AppException("Category not found with id: " + id, 404));
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param name - The category name
     * @return Category entity or null
     * @throws AppException if not found
     */
    @Override
    public Category getCategoryEntityByName(String name) {
        log.debug("Fetching category entity by name: {}", name);
        return categoryRepository.findByName(name)
            .orElseThrow(() -> new AppException("Category not found with name: " + name, 404));
    }

    /**
     * {@inheritDoc}
     * 
     * @param name - The category name
     * @return Category entity or null
     */
    @Override
    public Optional<Category> findCategoryEntityByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    // ================================================================
    // DTO METHODS - For REST API responses
    // ================================================================
    
    /**
     * {@inheritDoc}
     * 
     * @return List of CategoryResponseDTO
     *         Used for GET /api/categories
     */
    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        log.debug("Fetching all categories as DTOs");
        
        // Step 1: Get entities
        List<Category> categories = categoryRepository.findAll();
        
        // Step 2: Map to DTOs
        List<CategoryResponseDTO> dtos = mapper.toDtoList(categories);
        log.debug("Mapped {} categories to DTOs", dtos.size());
        
        return dtos;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param id - The category ID
     * @return CategoryDetailDTO
     * @throws AppException if not found
     */
    @Override
    public CategoryDetailDTO getCategoryById(Long id) {
        log.debug("Fetching category detail by id: {}", id);
        
        // Step 1: Get entity
        Category category = getCategoryEntityById(id);
        
        // Step 2: Map to detail DTO
        CategoryDetailDTO dto = mapper.toDetailDto(category);
        
        return dto;
    }

    @Override
    public CategoryDetailDTO getCategoryByName(String name) {
        log.debug("Fetching category detail by name: {}", name);
        
        // Step 1: Get entity
        Category category = getCategoryEntityByName(name);
        
        // Step 2: Map to detail DTO
        CategoryDetailDTO dto = mapper.toDetailDto(category);
        
        return dto;
    }
    
    // ================================================================
    // CRUD OPERATIONS - Write operations (Transactional)
    // ================================================================
    
    /**
     * {@inheritDoc}
     * 
     * @param request - Category data
     * @return Created CategoryDetailDTO
     * @throws AppException if name already exists
     */
    @Override
    @Transactional // Write operation
    public CategoryDetailDTO createCategory(CategoryRequestDTO request) {
        log.info("Creating new category with name: {}", request.name());
        
        // Step 1: Validate business rules
        validateUniqueName(request.name(), null);
        
        // Step 2: Map DTO → Entity
        Category category = mapper.toEntityFromDetail(request);
        log.debug("Mapped request to entity - name: {}", category.getName());
        
        // Step 3: Save
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        
        // Step 4: Map Entity → DTO (for response)
        CategoryDetailDTO response = mapper.toDetailDto(saved);
        
        return response;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param id - Category ID to update
     * @param request - Updated category data
     * @return Updated CategoryDetailDTO
     * @throws AppException if not found or name conflict
     */
    @Override
    @Transactional // Write operation
    public CategoryDetailDTO updateCategory(Long id, CategoryRequestDTO request) {
        log.info("Updating category with id: {}", id);
        log.debug("Update details - new name: {}", request.name());
        
        // Step 1: Check exists
        Category existingCategory = getCategoryEntityById(id);
        log.debug("Found existing category - id: {}, current name: {}", id, existingCategory.getName());
        
        // Step 2: Validate business rules
        if (!existingCategory.getName().equals(request.name())) {
            validateUniqueName(request.name(), id);
        }
        
        // Step 3: Update entity fields
        existingCategory.setName(request.name());
        
        // Step 4: Save
        Category updated = categoryRepository.save(existingCategory);
        log.info("Category updated successfully - id: {}, new name: {}", updated.getId(), updated.getName());
        
        // Step 5: Map Entity → DTO (for response)
        CategoryDetailDTO response = mapper.toDetailDto(updated);
        log.debug("Mapped updated entity to response DTO");
        
        return response;
    }
    
    /**
     * {@inheritDoc}
     * 
     * @param id - Category ID to delete
     * @throws AppException if not found or has associated vehicles
     */
    @Override
    @Transactional // Write operation
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        
        // Step 1: Check exists
        Category category = getCategoryEntityById(id);
        log.debug("Found category to delete - id: {}, name: {}", id, category.getName());
        
        // Step 2: Check if has associated vehicles
        // Debes agregar este método en tu repository
        // Commented out until VehicleRepository is available
        /*
        List<Vehicle> vehicles = vehicleRepository.findByCategoryId(id);
        if (!vehicles.isEmpty()) {
            log.warn("Cannot delete category {} - has {} associated vehicles", 
                category.getName(), vehicles.size());
            throw new AppException(
                String.format("Cannot delete category '%s' with %d associated vehicles. "
                    + "Reassign or delete the vehicles first.", 
                    category.getName(), vehicles.size()), 
                409
            );
        }
        */
        
        // Step 3: Delete
        categoryRepository.delete(category);
        log.info("Category deleted successfully - id: {}, name: {}", id, category.getName());
    }
    
    // ================================================================
    // UTILITY METHODS
    // ================================================================
    
    /**
     * {@inheritDoc}
     * 
     * @param name - Category name to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean existsByName(String name) {
        log.debug("Checking if category exists by name: {}", name);
        boolean exists = categoryRepository.existsByName(name);
        log.debug("Category exists: {} - {}", name, exists);
        return exists;
    }
    
    // ================================================================
    // PRIVATE HELPER METHODS
    // ================================================================
    
    /**
     * Validates that a category name is unique.
     * 
     * @param name - The name to validate
     * @param excludeId - ID to exclude from check (null for create)
     * @throws AppException if name already exists
     */
    private void validateUniqueName(String name, Long excludeId) {
        // Check if name exists in database
        if (categoryRepository.existsByName(name)) {
            // For update, we need to check if the name belongs to the same category
            if (excludeId != null) {
                Category existing = this.getCategoryEntityByName(name);
                
                if (!existing.getId().equals(excludeId)) {
                    log.warn("Category name conflict - name '{}' already exists (id: {})", 
                        name, existing.getId());
                    throw new AppException(
                        String.format("Category name '%s' is already used by another category (id: %d)", 
                            name, existing.getId()), 
                        409
                    );
                }

            } else {
                // Create operation
                log.warn("Duplicate category name rejected: {}", name);
                throw new AppException("Category name already exists: " + name, 409);
            }
        }
    }
}