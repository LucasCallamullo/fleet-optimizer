package com.routes.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;


public record LegRequestDTO(
    
    @NotNull(message = "Sequence is required")
    @Positive(message = "Sequence must be a positive number")
    Integer sequence,
    
    @NotNull(message = "Vehicle ID is required")
    @Positive(message = "Vehicle ID must be a positive number")
    Long vehicleId,
    
    @NotNull(message = "Package ID is required")
    @Positive(message = "Package ID must be a positive number")
    Long packageId,
    
    @Valid
    @NotNull(message = "Origin location is required")
    LocationRequestDTO origin,
    
    @Valid
    @NotNull(message = "Destination location is required")
    LocationRequestDTO destination
) {}