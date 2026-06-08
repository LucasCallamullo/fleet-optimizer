package com.fleets.controller;

import com.fleets.dto.response.VehicleResponseDTO;
import com.fleets.model.Vehicle;
import com.fleets.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for Vehicle endpoints.
 * Handles HTTP requests for vehicle operations.
 */
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
        return vehicleService.getAllVehicles().stream()
            .map(VehicleResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a vehicle by its ID.
     * GET /api/vehicles/{id}
     */
    @GetMapping("/{id}")
    public VehicleResponseDTO getVehicleById(@PathVariable Long id) {
        Vehicle vehicle = vehicleService.getVehicleById(id);
        return VehicleResponseDTO.fromEntity(vehicle);
    }
    
    /**
     * Creates a new vehicle.
     * POST /api/vehicles
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehicleResponseDTO createVehicle(@RequestBody Vehicle vehicle) {
        Vehicle created = vehicleService.createVehicle(vehicle);
        return VehicleResponseDTO.fromEntity(created);
    }
    
    /**
     * Updates an existing vehicle.
     * PUT /api/vehicles/{id}
     */
    @PutMapping("/{id}")
    public VehicleResponseDTO updateVehicle(@PathVariable Long id, @RequestBody Vehicle vehicle) {
        Vehicle updated = vehicleService.updateVehicle(id, vehicle);
        return VehicleResponseDTO.fromEntity(updated);
    }
    
    /**
     * Deletes a vehicle by ID.
     * DELETE /api/vehicles/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVehicle(@PathVariable Long id) {
        vehicleService.deleteVehicle(id);
    }
    
    /**
     * Retrieves all vehicles by category ID.
     * GET /api/vehicles/category/{categoryId}
     */
    @GetMapping("/category/{categoryId}")
    public List<VehicleResponseDTO> getVehiclesByCategory(@PathVariable Long categoryId) {
        return vehicleService.getVehiclesByCategory(categoryId).stream()
            .map(VehicleResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }
    
    /**
     * Retrieves a vehicle by its license plate.
     * GET /api/vehicles/license/{licensePlate}
     */
    @GetMapping("/license/{licensePlate}")
    public VehicleResponseDTO getVehicleByLicensePlate(@PathVariable String licensePlate) {
        Vehicle vehicle = vehicleService.getVehicleByLicensePlate(licensePlate);
        return VehicleResponseDTO.fromEntity(vehicle);
    }
}