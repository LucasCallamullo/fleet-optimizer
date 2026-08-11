package com.geocoding.dto.response;

import java.util.List;

/**
 * Response DTO for batch distance calculation.
 */
public record BatchDistanceResponseDTO(
    List<DistanceResultDTO> results
) {}
