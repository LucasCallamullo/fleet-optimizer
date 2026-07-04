package com.fleets.dto.response;

import java.time.LocalDateTime;
import com.fleets.model.VehicleStatus;

/**
 * DTO for Vehicle response.
 * Excludes the list of vehicles from Category to avoid circular reference.
 */
public record VehicleDetailDTO(
    Long id,
    String licensePlate,
    Integer year,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    CategoryResponseDTO category,

    // ================================================================
    // PHYSICAL CAPACITIES
    // ================================================================

    Double maxWeightKg,
    Double maxVolumeCbm,

    // ================================================================
    // EFFICIENCY AND COSTS
    // ================================================================

    Double fuelConsumptionPerKm,
    Double costPerKm,
    Double pricePerKm,

    // ================================================================
    // VEHICLE STATUS
    // ================================================================

    VehicleStatus status
) {

}