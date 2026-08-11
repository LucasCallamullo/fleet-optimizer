package com.geocoding.dto.request;

import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for distance calculation.
 */
public record DistanceRequestDTO(
    @NotNull(message = "Origin latitude is required")
    Double originLat,
    
    @NotNull(message = "Origin longitude is required")
    Double originLon,
    
    @NotNull(message = "Destination latitude is required")
    Double destLat, 
     
    @NotNull(message = "Destination longitude is required")
    Double destLon
) {}