package com.geocoding.dto.response;

/**
 * DTO representing the result of a distance calculation for a single leg.
 * 
 * Used in batch responses to return distance and duration for each leg,
 * correlated back to the original request via the legId.
 * 
 * @param legId Unique identifier that matches the original request's legId
 * @param distanceKm Calculated distance in kilometers
 * @param durationMinutes Estimated travel time in minutes
 */
public record DistanceResultDTO(
    Long legId,
    Double distanceKm,
    Integer durationMinutes
) {}