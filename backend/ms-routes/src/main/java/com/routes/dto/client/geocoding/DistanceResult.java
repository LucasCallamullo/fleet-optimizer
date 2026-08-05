package com.routes.dto.client.geocoding;

/**
 * Result for a single location pair.
 */
public record DistanceResult(
    Long legId,
    Double distanceKm,
    Integer durationMinutes
) {}