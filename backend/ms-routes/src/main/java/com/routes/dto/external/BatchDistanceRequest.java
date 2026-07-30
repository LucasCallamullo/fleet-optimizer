package com.routes.dto.external;

import java.util.List;

/**
 * Request DTO for batch distance calculation in ms-geocoding.
 */
public record BatchDistanceRequest(
    List<LocationPair> locations
) {}

