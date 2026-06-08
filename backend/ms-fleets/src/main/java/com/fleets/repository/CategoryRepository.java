package com.fleets.repository;

import com.fleets.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Category entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    /**
     * Find a category by its name.
     * @param name the category name (e.g., "Truck", "Car")
     * @return the category if found, null otherwise
     */
    Category findByName(String name);
    
    /**
     * Check if a category exists by name.
     * @param name the category name
     * @return true if exists, false otherwise
     */
    boolean existsByName(String name);
}