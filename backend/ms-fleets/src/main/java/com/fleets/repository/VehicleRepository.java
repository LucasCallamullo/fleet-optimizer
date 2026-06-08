package com.fleets.repository;

import com.fleets.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository interface for Vehicle entity.
 * Provides CRUD operations and custom query methods.
 */
@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    /**
     * Find all vehicles belonging to a specific category.
     * @param categoryId the category ID
     * @return list of vehicles in that category
     */
    List<Vehicle> findByCategoryId(Long categoryId);
    
    /**
     * Find a vehicle by its license plate (unique).
     * @param licensePlate the license plate number
     * @return the vehicle if found, null otherwise
     */
    Vehicle findByLicensePlate(String licensePlate);
    
    /**
     * Check if a vehicle exists with given license plate.
     * @param licensePlate the license plate number
     * @return true if exists, false otherwise
     */
    boolean existsByLicensePlate(String licensePlate);
    
    /**
     * Find all vehicles manufactured after a certain year.
     * @param year the minimum year
     * @return list of vehicles
     */
    List<Vehicle> findByYearAfter(Integer year);
}