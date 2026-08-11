package com.geocoding.dto.response;

public record DistanceResultDTO(
    Long legId,
    Double distanceKm,
    Integer durationMinutes
) {}
