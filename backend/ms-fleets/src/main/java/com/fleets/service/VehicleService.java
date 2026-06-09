package com.fleets.service;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.exception.AppException;
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
     * @throws AppException if vehicle not found (status 404)
     */
    Vehicle getVehicleById(Long id);
    
    /**
     * Creates a new vehicle from the request DTO.
     * @param request the DTO containing vehicle data
     * @return the created vehicle with generated ID
     * @throws AppException if license plate already exists (status 409)
     */
    Vehicle createVehicle(VehicleRequestDTO request);
    
    /**
     * Updates an existing vehicle from the request DTO.
     * @param id the ID of the vehicle to update
     * @param request the DTO containing updated vehicle data
     * @return the updated vehicle
     * @throws AppException if vehicle not found (status 404)
     * @throws AppException if license plate already exists on another vehicle (status 409)
     */
    Vehicle updateVehicle(Long id, VehicleRequestDTO request);
    
    /**
     * Deletes a vehicle by its ID.
     * @param id the ID of the vehicle to delete
     * @throws AppException if vehicle not found (status 404)
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