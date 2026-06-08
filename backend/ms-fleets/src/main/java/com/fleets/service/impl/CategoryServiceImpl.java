package com.fleets.service.impl;

import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;
import com.fleets.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * JPA-based implementation of CategoryService.
 * Uses Spring Data JPA for database operations.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
    
    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
    }
    
    @Override
    public Category getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }
    
    @Override
    public Category createCategory(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Category name already exists: " + category.getName());
        }
        return categoryRepository.save(category);
    }
    
    @Override
    public Category updateCategory(Long id, Category categoryDetails) {
        Category existingCategory = getCategoryById(id);
        
        // Check if new name conflicts with another category
        if (!existingCategory.getName().equals(categoryDetails.getName()) 
                && categoryRepository.existsByName(categoryDetails.getName())) {
            throw new RuntimeException("Category name already exists: " + categoryDetails.getName());
        }
        
        existingCategory.setName(categoryDetails.getName());
        return categoryRepository.save(existingCategory);
    }
    
    @Override
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        
        // Check if category has associated vehicles
        List<Vehicle> vehicles = vehicleRepository.findByCategoryId(id);
        if (!vehicles.isEmpty()) {
            throw new RuntimeException("Cannot delete category with associated vehicles. "
                    + "Reassign or delete " + vehicles.size() + " vehicle(s) first.");
        }
        
        categoryRepository.delete(category);
    }
    
    @Override
    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}