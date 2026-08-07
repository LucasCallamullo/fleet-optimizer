package com.packages.dto.external;

/**
 * Location DTO for inter-service communication.
 * 
 * This DTO is used exclusively for communication between microservices.
 * It is separate from LocationRequestDTO (API) to avoid coupling.
 * 
 * Contains only the fields needed for geocoding and route calculation.
 * No validation annotations (validation is handled by the receiving service).
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
) {
    /**
     * Creates a LocationDTO with default country "Argentina" if not provided.
     */
    public LocationDTO {
        if (country == null || country.isBlank()) {
            country = "Argentina";
        }
    }
}