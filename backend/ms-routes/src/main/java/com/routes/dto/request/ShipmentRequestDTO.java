package com.routes.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for creating a shipment.
 * 
 * A shipment is a collection of packages that will be delivered
 * to a single destination using a single vehicle.
 * 
 * Business Rules:
 * - All packages must be READY_FOR_PICKUP
 * - Vehicle must have sufficient capacity (weight + volume)
 * - All packages must fit in the vehicle
 */
public record ShipmentRequestDTO(
    
    /**
     * List of package IDs to ship.
     * Must contain at least one package.
     */
    @NotEmpty(message = "At least one package is required")
    List<Long> packageIds,
    
    /**
     * ID of the vehicle to use for delivery.
     * Must be available and have sufficient capacity.
     */
    @NotNull(message = "Vehicle ID is required")
    Long vehicleId,
    
    /**
     * Destination coordinates for the delivery.
     * This is where all packages will be delivered.
     */
    @NotNull(message = "Destination is required")
    LocationRequestDTO destination
) {}