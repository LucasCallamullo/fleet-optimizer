package com.fleets.service;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleResponseDTO;
import com.fleets.dto.response.VehicleDetailDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Vehicle;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for Vehicle business logic.
 * Defines the contract for vehicle operations.
 * 
 * ================================================================
 * PATTERN: Two-level API
 * ================================================================
 * 
 * ENTITY methods (return JPA Entities):
 * - For internal use by other services
 * - Give full access to all entity fields
 * - Used when other services need to manipulate entities
 * 
 * DTO methods (return DTOs):
 * - For REST API responses
 * - Only expose needed fields
 * - Decouple API from entity model
 * 
 * ================================================================
 */
public interface VehicleService {
    
    // ================================================================
    // ENTITY METHODS - For internal use
    // ================================================================
    
    /**
     * Retrieves all vehicles as JPA entities.
     * Used internally by other services that need to manipulate entities.
     * 
     * @return list of all Vehicle entities
     */
    List<Vehicle> getAllVehiclesEntity();
    
    /**
     * Finds a basic vehicle entity by its unique ID (LAZY loading).
     * Category proxy is not initialized. Use for basic operations.
     * 
     * @param id the vehicle ID. 
     * @return the Vehicle entity if found.
     * @throws AppException if vehicle not found (status 404).
     */
    Vehicle getVehicleEntityById(Long id);

    /**
     * Finds a vehicle entity by its unique ID.
     * 
     * @param id the vehicle ID
     * @return the Vehicle entity if found
     * @throws AppException if vehicle not found (status 404)
     */
    Vehicle getVehicleEntityWithCategoryById(Long id);
    
    /**
     * Finds a vehicle entity by its license plate.
     * 
     * @param licensePlate the license plate number
     * @return Optional containing the Vehicle entity if found
     */
    Optional<Vehicle> getVehicleEntityByLicensePlate(String licensePlate);
    
    // ================================================================
    // DTO METHODS - For REST API responses
    // ================================================================
    
    /**
     * Retrieves all vehicles as DTOs for the REST API.
     * Includes only category ID (no category details).
     * Optimized for listing endpoints.
     * 
     * @return list of VehicleResponseDTO
     */
    List<VehicleResponseDTO> getAllVehicles();
    
    /**
     * Retrieves all vehicles with full category details.
     * Uses JOIN FETCH to load category data.
     * 
     * @return list of VehicleDetailDTO
     */
    List<VehicleDetailDTO> getAllVehiclesWithCategory();
    
    /**
     * Finds a vehicle by ID and returns it as a Detail DTO.
     * Includes full category information.
     * 
     * @param id the vehicle ID
     * @return VehicleDetailDTO with all fields
     * @throws AppException if vehicle not found (status 404)
     */
    VehicleDetailDTO getVehicleById(Long id);
    
    /**
     * Finds a vehicle by its license plate.
     * Includes full category information.
     * 
     * @param licensePlate the license plate number
     * @return VehicleDetailDTO if found, null otherwise
     */
    VehicleDetailDTO getVehicleByLicensePlate(String licensePlate);
    
    /**
     * Finds all vehicles belonging to a specific category.
     * 
     * @param categoryId the category ID
     * @return list of VehicleDetailDTO in that category
     */
    List<VehicleDetailDTO> getVehiclesByCategory(Long categoryId);

    /**
     * Finds available vehicles that meet the weight and volume requirements.
     * 
     * Criteria:
     * 1. Status must be AVAILABLE
     * 2. maxWeightKg must be null OR >= requiredWeightKg
     * 3. maxVolumeCbm must be null OR >= requiredVolumeCbm
     * 
     * @param requiredWeightKg - Required weight capacity
     * @param requiredVolumeCbm - Required volume capacity
     * @return List of matching vehicles
     */
    List<VehicleResponseDTO> findAvailableVehicles(Double requiredWeightKg, Double requiredVolumeCbm);
    
    // ================================================================
    // CRUD OPERATIONS
    // ================================================================
    
    /**
     * Creates a new vehicle from the request DTO.
     * 
     * @param request the DTO containing vehicle data
     * @return the created VehicleDetailDTO
     * @throws AppException if license plate already exists (status 409)
     */
    VehicleDetailDTO createVehicle(VehicleRequestDTO request);
    
    /**
     * Updates an existing vehicle from the request DTO.
     * 
     * @param id the ID of the vehicle to update
     * @param request the DTO containing updated vehicle data
     * @return the updated VehicleDetailDTO
     * @throws AppException if vehicle not found (status 404)
     * @throws AppException if license plate already exists on another vehicle (status 409)
     */
    VehicleDetailDTO updateVehicle(Long id, VehicleRequestDTO request);
    
    /**
     * Deletes a vehicle by its ID.
     * 
     * @param id the ID of the vehicle to delete
     * @throws AppException if vehicle not found (status 404)
     */
    void deleteVehicle(Long id);
    
}