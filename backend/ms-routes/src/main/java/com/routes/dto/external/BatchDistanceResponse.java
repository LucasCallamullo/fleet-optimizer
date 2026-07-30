package com.routes.dto.external;

import java.util.List;

/**
 * Response DTO for batch distance calculation.
 */
public record BatchDistanceResponse(
    List<DistanceResult> results
) {}

