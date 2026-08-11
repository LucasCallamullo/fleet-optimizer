package com.geocoding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Request DTO for batch distance calculation.
 * 
 * This DTO is used by ms-routes (via Feign Client) to calculate distances
 * for multiple legs in a single HTTP request.
 * 
 * Each pair contains an origin and destination coordinate pair,
 * identified by a legId that maps the response back to the original request.
 * 
 * Example usage from ms-routes:
 * <pre>
 * BatchDistanceRequestDTO request = new BatchDistanceRequestDTO(
 *     List.of(
 *         new LocationPairDTO(1L, -34.6037, -58.3816, -31.4201, -64.1888),
 *         new LocationPairDTO(2L, -31.4201, -64.1888, -32.8908, -68.8272)
 *     )
 * );
 * 
 * BatchDistanceResponseDTO response = geocodingClient.calculateBatchDistances(request);
 * </pre>
 * 
 * @param pairs List of location pairs to calculate distances for
 * @see LocationPairDTO
 * @see com.geocoding.dto.response.BatchDistanceResponseDTO
 */
public record BatchDistanceRequestDTO(
    @NotEmpty(message = "At least one location pair is required")
    @Valid
    List<LocationPairDTO> locations
) {}