package com.geocoding.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

/**
 * Request DTO for batch distance calculation.
 */
public record BatchDistanceRequestDTO(
    @NotEmpty(message = "At least one location pair is required")
    @Valid
    List<LocationPairDTO> pairs
) {}

