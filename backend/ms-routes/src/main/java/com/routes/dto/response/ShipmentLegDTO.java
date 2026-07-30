package com.routes.dto.response;

import com.routes.model.enums.LegStatus;
import com.routes.model.entity.Location;

import java.time.LocalDateTime;

/**
 * DTO for leg information within a shipment response.
 * Contains package details and tracking information.
 */
public record ShipmentLegDTO(
    Long legId,
    Integer sequence,
    LegStatus status,
    Double distanceKm,
    Integer durationMinutes,
    Long vehicleId,
    Long packageId,
    Double weightKg,
    Double volumeCbm,
    Location origin,
    Location destination,
    LocalDateTime estimatedArrival
) {}
