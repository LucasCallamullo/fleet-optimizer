package com.fleets.dto.response;

import com.fleets.model.VehicleStatus;
import java.time.LocalDateTime;

/**
 * DTO for Vehicle response (basic version).
 * Used for list endpoints where full details are not needed.
 * Contains only essential fields and category ID reference.
 */
public record VehicleResponseDTO(
    Long id,
    String licensePlate,
    Integer year,
    LocalDateTime updatedAt,
    Long categoryId,

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