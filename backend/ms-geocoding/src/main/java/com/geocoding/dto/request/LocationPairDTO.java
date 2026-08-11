package com.geocoding.dto.request;

public record LocationPairDTO(
    Long legId,
    Double originLat,
    Double originLon,
    Double destLat,
    Double destLon
) {}