package com.routes.dto.client.geocoding;

import java.util.List;

/**
 * Request DTO for batch distance calculation in ms-geocoding.
 */
public record BatchDistanceRequest(
    List<LocationPair> locations
) {}

