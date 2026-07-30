package com.routes.dto.external;

/**
 * Result for a single location pair.
 */
public record DistanceResult(
    Long legId,
    Double distanceKm,
    Integer durationMinutes
) {}