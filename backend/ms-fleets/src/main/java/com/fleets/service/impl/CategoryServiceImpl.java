package com.fleets.service.impl;

import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;
import com.fleets.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * JPA-based implementation of CategoryService.
 * Uses Spring Data JPA for database operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    
    @Override
    public List<Category> getAllCategories() {
        log.debug("Fetching all categories from database");
        List<Category> categories = categoryRepository.findAll();
        log.debug("Found {} categories in database", categories.size());
        return categories;
    }
    
    @Override
    public Category getCategoryById(Long id) {
        log.debug("Fetching category by id: {}", id);
        return categoryRepository.findById(id)
            .orElseThrow(() -> new AppException("Category not found with id: " + id, 404));
    }
    
    @Override
    public Category getCategoryByName(String name) {
        log.debug("Fetching category by name: {}", name);
        return categoryRepository.findByName(name);
    }
    
    @Override
    public Category createCategory(CategoryRequestDTO request) {
        log.info("Creating new category with name: {}", request.getName());
        
        if (categoryRepository.existsByName(request.getName())) {
            log.warn("Duplicate category name rejected: {}", request.getName());
            throw new AppException("Category name already exists: " + request.getName(), 409);
        }
        
        Category category = request.toEntity();
        Category saved = categoryRepository.save(category);
        log.info("Category created successfully with id: {}", saved.getId());
        return saved;
    }
    
    @Override
    public Category updateCategory(Long id, CategoryRequestDTO request) {
        log.info("Updating category with id: {}", id);
        log.debug("Update details - new name: {}", request.getName());
        
        Category existingCategory = getCategoryById(id);
        
        // Check if new name conflicts with another category
        if (!existingCategory.getName().equals(request.getName()) 
                && categoryRepository.existsByName(request.getName())) {
            log.warn("Category name conflict - name '{}' already exists", request.getName());
            throw new AppException("Category name already exists: " + request.getName(), 409);
        }
        
        existingCategory.setName(request.getName());
        Category updated = categoryRepository.save(existingCategory);
        log.info("Category updated successfully - id: {}, new name: {}", updated.getId(), updated.getName());
        return updated;
    }
    
    @Override
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        
        Category category = getCategoryById(id);
        
        // Check if category has associated vehicles
        List<Vehicle> vehicles = vehicleRepository.findByCategoryId(id);
        if (!vehicles.isEmpty()) {
            log.warn("Cannot delete category {} - has {} associated vehicles", category.getName(), vehicles.size());
            throw new AppException("Cannot delete category with associated vehicles. "
                    + "Reassign or delete " + vehicles.size() + " vehicle(s) first.", 409);
        }
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully - id: {}, name: {}", id, category.getName());
    }
    
    @Override
    public boolean existsByName(String name) {
        log.debug("Checking if category exists by name: {}", name);
        boolean exists = categoryRepository.existsByName(name);
        log.debug("Category exists: {} - {}", name, exists);
        return exists;
    }
}