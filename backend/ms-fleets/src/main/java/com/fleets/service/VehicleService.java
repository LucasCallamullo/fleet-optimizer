package com.fleets.service;

import com.fleets.model.Vehicle;
import java.util.List;

/**
 * Service interface for Vehicle business logic.
 * Defines the contract for vehicle operations.
 */
public interface VehicleService {
    
    /**
     * Retrieves all vehicles from the system.
     * @return list of all vehicles
     */
    List<Vehicle> getAllVehicles();
    
    /**
     * Finds a vehicle by its unique ID.
     * @param id the vehicle ID
     * @return the vehicle if found
     * @throws RuntimeException if vehicle not found
     */
    Vehicle getVehicleById(Long id);
    
    /**
     * Creates a new vehicle.
     * @param vehicle the vehicle to create
     * @return the created vehicle with generated ID
     * @throws RuntimeException if license plate already exists
     */
    Vehicle createVehicle(Vehicle vehicle);
    
    /**
     * Updates an existing vehicle.
     * @param id the ID of the vehicle to update
     * @param vehicle the updated vehicle data
     * @return the updated vehicle
     * @throws RuntimeException if vehicle not found
     */
    Vehicle updateVehicle(Long id, Vehicle vehicle);
    
    /**
     * Deletes a vehicle by its ID.
     * @param id the ID of the vehicle to delete
     * @throws RuntimeException if vehicle not found
     */
    void deleteVehicle(Long id);
    
    /**
     * Finds all vehicles belonging to a specific category.
     * @param categoryId the category ID
     * @return list of vehicles in that category
     */
    List<Vehicle> getVehiclesByCategory(Long categoryId);
    
    /**
     * Finds a vehicle by its license plate.
     * @param licensePlate the license plate number
     * @return the vehicle if found, null otherwise
     */
    Vehicle getVehicleByLicensePlate(String licensePlate);
}