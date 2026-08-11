package com.geocoding.dto.response;

/**
 * Response DTO for single distance calculation.
 * 
 * This DTO is returned by ms-geocoding to ms-routes when calculating
 * the distance for a single leg.
 * 
 * The geometry field contains the full route path as GeoJSON (LineString),
 * which can be used by the frontend to draw the route on a map.
 * 
 * Example response for Buenos Aires → Cordoba:
 * {
 *   "distanceKm": 700.5,
 *   "durationMinutes": 480,
 *   "geometry": "{\"type\":\"LineString\",\"coordinates\":[[...]]}"
 * }
 * 
 * @param distanceKm Calculated distance in kilometers
 * @param durationMinutes Estimated travel time in minutes
 * @param geometry Route path as GeoJSON LineString (optional, can be null)
 */
public record DistanceResponseDTO(
    Double distanceKm,
    Integer durationMinutes,
    String geometry
) {}