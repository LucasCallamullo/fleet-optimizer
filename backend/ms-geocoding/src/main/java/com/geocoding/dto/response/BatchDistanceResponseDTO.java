package com.geocoding.dto.response;

import java.util.List;

/**
 * Response DTO for batch distance calculation.
 * 
 * This DTO is returned by ms-geocoding to ms-routes when calculating
 * distances for multiple legs in a single request.
 * 
 * Each result corresponds to a leg from the original request, matched
 * by the legId field. This allows ms-routes to assign each calculated
 * distance to the correct Leg entity.
 * 
 * Example response for 2 legs:
 * {
 *   "results": [
 *     { "legId": 1, "distanceKm": 700.5, "durationMinutes": 480 },
 *     { "legId": 2, "distanceKm": 650.3, "durationMinutes": 420 }
 *   ]
 * }
 * 
 * @param results List of distance results for each leg
 * @see DistanceResultDTO
 */
public record BatchDistanceResponseDTO(
    List<DistanceResultDTO> results
) {}