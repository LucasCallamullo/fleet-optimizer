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
) {
    /**
     * Creates a LocationDTO with only coordinates.
     * All address fields are null.
     */
    public static LocationDTO ofCoordinates(Double latitude, Double longitude) {
        return new LocationDTO(null, null, null, null, null, null, latitude, longitude);
    }
}
