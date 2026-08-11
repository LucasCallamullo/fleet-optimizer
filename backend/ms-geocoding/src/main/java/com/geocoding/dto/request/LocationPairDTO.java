package com.geocoding.dto.request;

import com.geocoding.dto.common.LocationDTO;

/**
 * DTO representing a pair of locations for distance calculation.
 * 
 * Used by ms-routes to request distance calculation for a single leg.
 * The legId is used to correlate the response back to the original leg.
 * 
 * Coordinates are expected in decimal degrees (WGS84 standard):
 * - Latitude: -90.0 to 90.0 (negative = South)
 * - Longitude: -180.0 to 180.0 (negative = West)
 * 
 * Example: Buenos Aires (-34.6037, -58.3816)
 * 
 * @param legId Unique identifier for this leg (maps to Leg.id in ms-routes)
 * @param originLat Latitude of the origin location
 * @param originLon Longitude of the origin location
 * @param destLat Latitude of the destination location
 * @param destLon Longitude of the destination location
 */

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record LocationPairDTO(
    Long legId,
    @NotNull @Valid LocationDTO origin,
    @NotNull @Valid LocationDTO destination
) {}