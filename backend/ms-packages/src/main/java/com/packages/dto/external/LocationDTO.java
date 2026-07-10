package com.packages.dto.external;

/**
 * Location DTO for inter-service communication.
 * Contains address and coordinates needed for route calculation.
 */
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