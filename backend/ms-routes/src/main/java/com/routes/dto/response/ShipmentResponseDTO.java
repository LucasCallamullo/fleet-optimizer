package com.routes.dto.response;

import com.routes.model.enums.RouteStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for a created shipment.
 */
public record ShipmentResponseDTO(
    Long routeId,
    String routeName,
    RouteStatus status,
    Long vehicleId,
    Double totalDistanceKm,
    Integer totalDurationMinutes,
    List<ShipmentLegDTO> legs,
    LocalDateTime estimatedArrival,
    LocalDateTime createdAt
) {}