package com.fleets.controller;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleResponseDTO;
import com.fleets.model.Vehicle;
import com.fleets.service.VehicleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Vehicle endpoints.
 * Handles HTTP requests for vehicle operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
public class VehicleController {
    
    private final VehicleService vehicleService;
    
    /**
     * Retrieves all vehicles.
     * GET /api/vehicles
     */
    @GetMapping
    public List<VehicleResponseDTO> getAllVehicles() {
        log.info("GET /api/vehicles - Fetching all vehicles");
        List<VehicleResponseDTO> result = vehicleService.getAllVehicles().stream()
            .map(VehicleResponseDTO::fromEntity)
            .collect(Collectors.toList());
        log.info("GET /api/vehicles - Returned {} vehicles", result.size());
        return result;
    }
    
    /**
     * Retrieves a vehicle by its ID.
     * GET /api/vehicles/{id}
     */
    @GetMapping("/{id}")
    public VehicleResponseDTO getVehicleById(@PathVariable Long id) {
        log.info("GET /api/vehicles/{} - Fetching vehicle", id);
        Vehicle vehicle = vehicleService.getVehicleById(id);
        log.debug("Vehicle found - id: {}, plate: {}", vehicle.getId(), vehicle.getLicensePlate());
        return VehicleResponseDTO.fromEntity(vehicle);
    }

    /**
     * Creates a new vehicle.
     * POST /api/vehicles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponseDTO createVehicle(@Valid @RequestBody VehicleRequestDTO request) {
        log.info("POST /api/vehicles - Creating new vehicle with plate: {}", request.getLicensePlate());
        Vehicle created = vehicleService.createVehicle(request);
        log.info("Vehicle created successfully - id: {}", created.getId());
        return VehicleResponseDTO.fromEntity(created);
    }
    
    /**
     * Updates an existing vehicle.
     * PUT /api/vehicles/{id}
     */
    @PutMapping("/{id}")
    public VehicleResponseDTO updateVehicle(@PathVariable Long id, @Valid @RequestBody VehicleRequestDTO request) {
        log.info("PUT /api/vehicles/{} - Updating vehicle", id);
        Vehicle updated = vehicleService.updateVehicle(id, request);
        log.info("Vehicle updated successfully - id: {}", updated.getId());
        return VehicleResponseDTO.fromEntity(updated);
    }
    
    /**
     * Deletes a vehicle by ID.
     * DELETE /api/vehicles/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        log.info("DELETE /api/vehicles/{} - Deleting vehicle", id);
        vehicleService.deleteVehicle(id);
        log.info("Vehicle deleted successfully - id: {}", id);
    }
    
    /**
     * Retrieves all vehicles by category ID.
     * GET /api/vehicles/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public List<VehicleResponseDTO> getVehiclesByCategory(@PathVariable Long categoryId) {
        log.info("GET /api/vehicles/category/{} - Fetching vehicles by category", categoryId);
        List<VehicleResponseDTO> result = vehicleService.getVehiclesByCategory(categoryId).stream()
            .map(VehicleResponseDTO::fromEntity)
            .collect(Collectors.toList());
        log.info("GET /api/vehicles/category/{} - Returned {} vehicles", categoryId, result.size());
        return result;
    }
    
    /**
     * Retrieves a vehicle by its license plate.
     * GET /api/vehicles/license/{licensePlate}
     */
    @GetMapping("/license/{licensePlate}")
    public VehicleResponseDTO getVehicleByLicensePlate(@PathVariable String licensePlate) {
        log.info("GET /api/vehicles/license/{} - Fetching vehicle by license plate", licensePlate);
        Vehicle vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        if (vehicle == null) {
            log.warn("Vehicle not found with license plate: {}", licensePlate);
            throw new RuntimeException("Vehicle not found with license plate: " + licensePlate);
        }
        log.debug("Vehicle found - id: {}, plate: {}", vehicle.getId(), vehicle.getLicensePlate());
        return VehicleResponseDTO.fromEntity(vehicle);
    }
}