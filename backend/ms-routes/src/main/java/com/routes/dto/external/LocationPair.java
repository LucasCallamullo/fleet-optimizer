package com.routes.dto.external;

import com.routes.dto.request.LocationRequestDTO;

/**
 * A pair of origin and destination locations.
 */
public record LocationPair(
    Long legId,           // Optional: to correlate response with leg
    LocationRequestDTO origin,
    LocationRequestDTO destination
) {}