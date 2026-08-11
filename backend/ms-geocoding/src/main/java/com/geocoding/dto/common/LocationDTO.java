package com.geocoding.dto.common;

public record LocationDTO(
    String street,
    String streetNumber,
    String city,
    String state,
    String country,
    String postalCode,
    Double latitude,
    Double longitude
) {}
