package com.geocoding.dto.response;

/**
 * Response DTO for distance calculation.
 */
public record DistanceResponseDTO(
    Double distanceKm,
    Integer durationMinutes,
    String geometry  // Opcional: para dibujar en mapa (GeoJSON)
) {}