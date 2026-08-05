package com.routes.dto.client.geocoding;

import com.routes.dto.client.common.LocationDTO;

/**
 * A pair of origin and destination locations.
 */
public record LocationPair(
    Long legId,           // Optional: to correlate response with leg
    LocationDTO origin,
    LocationDTO destination
) {}