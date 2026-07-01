package com.fleets.repository;

import com.fleets.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;


@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    
    // ================================================================
    // FIND BY CATEGORY ID
    // ================================================================
    
    /**
     * Find all vehicles belonging to a specific category.
     * Category is NOT loaded (LAZY).
     * Use this for basic list views where category details are not needed.
     * 
     * Spring Data JPA parses this method name and generates:
     * SELECT v FROM Vehicle v WHERE v.category.id = ?1
     * 
     * @param categoryId the category ID
     * @return list of vehicles (category is LAZY, not loaded)
     */
    List<Vehicle> findByCategoryId(Long categoryId);
    
    /**
     * Find all vehicles belonging to a specific category with category loaded.
     * Uses JOIN FETCH to load category in the same query (EAGER).
     * Use this when you need to display category details.
     * 
     * @param categoryId the category ID
     * @return list of vehicles with category loaded (EAGER)
     */
    @Query("SELECT v FROM Vehicle v JOIN FETCH v.category WHERE v.category.id = :categoryId")
    List<Vehicle> findByCategoryIdWithCategory(@Param("categoryId") Long categoryId);
    
    // ================================================================
    // FIND ALL
    // ================================================================
    
    /**
     * Find all vehicles without loading categories (LAZY).
     * Use this for list views where only vehicle data is needed.
     * 
     * @return list of all vehicles (category is LAZY)
     */
    @Query("SELECT v FROM Vehicle v")
    List<Vehicle> findAllBasic();
    
    /**
     * Find all vehicles with categories loaded (EAGER), including those without a category.
     * Uses LEFT JOIN FETCH to ensure vehicles with null categories are not excluded.
     * * @return list of all vehicles (with or without category loaded)
     */
    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.category")
    List<Vehicle> findAllWithCategory();
    
    // ================================================================
    // FIND BY ID
    // ================================================================
    
    /**
     * Find a vehicle by its ID (LAZY).
     * Inherited from JpaRepository.
     * Category is NOT loaded.
     * Use this for basic operations where category is not needed.
     * 
     * @param id the vehicle ID
     * @return Optional containing the vehicle if found
     */
    // Optional<Vehicle> findById(Long id); // Already provided by JpaRepository
    
    /**
     * Find a vehicle by its ID with category loaded (EAGER).
     * Uses JOIN FETCH to load category in the same query.
     * Use this when you need to display category details.
     * 
     * @param id the vehicle ID
     * @return Optional containing the vehicle with category loaded (EAGER)
     */
    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.category WHERE v.id = :id")
    Optional<Vehicle> findByIdWithCategory(@Param("id") Long id);
    
    // ================================================================
    // FIND BY LICENSE PLATE
    // ================================================================
    
    /**
     * Find a vehicle by its license plate (LAZY).
     * Category is NOT loaded.
     * Use this for checking existence or basic operations.
     * 
     * @param licensePlate the license plate number
     * @return Optional containing the vehicle if found (category is LAZY)
     */
    Optional<Vehicle> findByLicensePlate(String licensePlate);
    
    /**
     * Find a vehicle by its license plate with category loaded (EAGER).
     * Uses JOIN FETCH to load category in the same query.
     * Use this when you need to display category details.
     * 
     * @param licensePlate the license plate number
     * @return Optional containing the vehicle with category loaded (EAGER)
     */
    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.category WHERE v.licensePlate = :licensePlate")
    Optional<Vehicle> findByLicensePlateWithCategory(@Param("licensePlate") String licensePlate);
    
    // ================================================================
    // FIND BY YEAR
    // ================================================================
    
    /**
     * Find all vehicles manufactured after a certain year (LAZY).
     * Category is NOT loaded.
     * Use this for basic list views.
     * 
     * @param year the minimum year
     * @return list of vehicles (category is LAZY)
     */
    List<Vehicle> findByYearAfter(Integer year);
    
    /**
     * Find all vehicles manufactured after a certain year with category loaded (EAGER).
     * Uses JOIN FETCH to load category in the same query.
     * Use this when you need to display category details.
     * 
     * @param year the minimum year
     * @return list of vehicles with category loaded (EAGER)
     */
    @Query("SELECT v FROM Vehicle v LEFT JOIN FETCH v.category WHERE v.year > :year")
    List<Vehicle> findByYearAfterWithCategory(@Param("year") Integer year);
    
    // ================================================================
    // EXISTENCE CHECKS
    // ================================================================
    
    /**
     * Check if a vehicle exists with given license plate.
     * This is a derived query method.
     * 
     * @param licensePlate the license plate number
     * @return true if exists, false otherwise
     */
    boolean existsByLicensePlate(String licensePlate);
}