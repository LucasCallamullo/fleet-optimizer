package com.fleets.controller;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleResponseDTO;
import com.fleets.dto.response.VehicleDetailDTO;
import com.fleets.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
// import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Vehicle endpoints.
 * Handles HTTP requests for vehicle operations.
 * 
 * ================================================================
 * RESPONSIBILITIES:
 * ================================================================
 * 
 * 1. Receive HTTP requests
 * 2. Validate input (via @Valid)
 * 3. Call the appropriate service method
 * 4. Return the response (DTOs)
 * 5. Handle HTTP status codes
 * 
 * The controller is THIN - all business logic is in the service layer.
 * 
 * ================================================================
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    // ================================================================
    // GET ENDPOINTS
    // ================================================================
    
    /**
     * Retrieves all vehicles (basic info).
     * GET /api/v1/vehicles
     * 
     * @return List of VehicleResponseDTO (only category ID)
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<VehicleResponseDTO> getAllVehicles() {
        log.info("GET /api/v1/vehicles - Fetching all vehicles (basic)");
        List<VehicleResponseDTO> result = vehicleService.getAllVehicles();
        log.info("GET /api/v1/vehicles - Returned {} vehicles", result.size());
        return result;
    }
    
    /**
     * Retrieves all vehicles with full category details.
     * GET /api/v1/vehicles/detailed
     * 
     * @return List of VehicleDetailDTO (with full category)
     */
    @GetMapping("/detailed")
    @PreAuthorize("isAuthenticated()")
    public List<VehicleDetailDTO> getAllVehiclesDetailed() {
        log.info("GET /api/v1/vehicles/detailed - Fetching all vehicles with category details");
        List<VehicleDetailDTO> result = vehicleService.getAllVehiclesWithCategory();
        log.info("GET /api/v1/vehicles/detailed - Returned {} vehicles", result.size());
        return result;
    }
    
    /**
     * Retrieves a vehicle by its ID with full details.
     * GET /api/v1/vehicles/{id}
     * 
     * @param id the vehicle ID
     * @return VehicleDetailDTO with full category
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public VehicleDetailDTO getVehicleById(@PathVariable Long id) {
        log.info("GET /api/v1/vehicles/{} - Fetching vehicle detail", id);
        VehicleDetailDTO result = vehicleService.getVehicleById(id);
        log.debug("Vehicle found - id: {}, plate: {}", result.id(), result.licensePlate());
        return result;
    }
    
    /**
     * Retrieves a vehicle by its license plate with full details.
     * GET /api/v1/vehicles/license/{licensePlate}
     * 
     * @param licensePlate the license plate number
     * @return VehicleDetailDTO with full category
     */
    @GetMapping("/license/{licensePlate}")
    @PreAuthorize("isAuthenticated()")
    public VehicleDetailDTO getVehicleByLicensePlate(@PathVariable String licensePlate) {
        log.info("GET /api/v1/vehicles/license/{} - Fetching vehicle by license plate", licensePlate);
        VehicleDetailDTO result = vehicleService.getVehicleByLicensePlate(licensePlate);
        log.debug("Vehicle found - id: {}, plate: {}", result.id(), result.licensePlate());
        return result;
    }
    
    /**
     * Retrieves all vehicles by category ID with full details.
     * GET /api/v1/vehicles/category/{categoryId}
     * 
     * @param categoryId the category ID
     * @return List of VehicleDetailDTO with full category
     */
    @GetMapping("/category/{categoryId}")
    @PreAuthorize("isAuthenticated()")
    public List<VehicleDetailDTO> getVehiclesByCategory(@PathVariable Long categoryId) {
        log.info("GET /api/v1/vehicles/category/{} - Fetching vehicles by category", categoryId);
        List<VehicleDetailDTO> result = vehicleService.getVehiclesByCategory(categoryId);
        log.info("GET /api/v1/vehicles/category/{} - Returned {} vehicles", categoryId, result.size());
        return result;
    }
    
    // ================================================================
    // CRUD OPERATIONS
    // ================================================================
    
    /**
     * Creates a new vehicle.
     * POST /api/v1/vehicles
     * 
     * @param request the vehicle data
     * @return VehicleDetailDTO with created vehicle
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleDetailDTO createVehicle(@Valid @RequestBody VehicleRequestDTO request) {
        log.info("POST /api/v1/vehicles - Creating new vehicle with plate: {}", request.getLicensePlate());
        VehicleDetailDTO result = vehicleService.createVehicle(request);
        log.info("Vehicle created successfully - id: {}, plate: {}", result.id(), result.licensePlate());
        return result;
    }
    
    /**
     * Updates an existing vehicle.
     * PUT /api/v1/vehicles/{id}
     * 
     * @param id the vehicle ID
     * @param request the updated vehicle data
     * @return VehicleDetailDTO with updated vehicle
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public VehicleDetailDTO updateVehicle(
            @PathVariable Long id, 
            @Valid @RequestBody VehicleRequestDTO request) {
        log.info("PUT /api/v1/vehicles/{} - Updating vehicle", id);
        VehicleDetailDTO result = vehicleService.updateVehicle(id, request);
        log.info("Vehicle updated successfully - id: {}", result.id());
        return result;
    }
    
    /**
     * Deletes a vehicle by ID.
     * DELETE /api/v1/vehicles/{id}
     * 
     * @param id the vehicle ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteVehicle(@PathVariable Long id) {
        log.info("DELETE /api/v1/vehicles/{} - Deleting vehicle", id);
        vehicleService.deleteVehicle(id);
        log.info("Vehicle deleted successfully - id: {}", id);
    }

    /**
     * GET /api/v1/vehicles/available-for-package
     * 
     * Returns vehicles that can carry a package with given requirements.
     * 
     * @param requiredWeightKg - Required weight capacity in kg
     * @param requiredVolumeCbm - Required volume capacity in m³
     * @return List of available vehicles that meet the requirements
     */
    @GetMapping("/available-for-package")
    @PreAuthorize("isAuthenticated()")
    public List<VehicleResponseDTO> getAvailableVehiclesForPackage(
            @RequestParam Double requiredWeightKg,
            @RequestParam Double requiredVolumeCbm) {
        
        log.info("GET /api/v1/vehicles/available-for-package - Weight: {}kg, Volume: {}m³", 
            requiredWeightKg, requiredVolumeCbm);
        
        List<VehicleResponseDTO> vehicles = vehicleService.findAvailableVehicles(
            requiredWeightKg, requiredVolumeCbm);
        
        return vehicles;
    }
}