package com.routes.dto.request;

import jakarta.validation.constraints.*;

public record LocationRequestDTO(
    
    @NotBlank(message = "Street is required")
    String street,
    
    String streetNumber,
    
    @NotBlank(message = "City is required")
    String city,
    
    String state,
    
    @NotBlank(message = "Country is required")
    String country,
    
    String postalCode,
    
    @NotNull(message = "Latitude is required for distance calculation")
    Double latitude,
    
    @NotNull(message = "Longitude is required for distance calculation")
    Double longitude
) {
    // ================================================================
    // COMPACT CONSTRUCTOR - For default values and normalization
    // ================================================================
    
    public LocationRequestDTO {
        // If country is null or empty, default to "Argentina"
        if (country == null || country.isBlank()) {
            country = "Argentina";
        }
        
        // Normalize: trim whitespace
        if (street != null) street = street.trim();
        if (streetNumber != null) streetNumber = streetNumber.trim();
        if (city != null) city = city.trim();
        if (state != null) state = state.trim();
        if (postalCode != null) postalCode = postalCode.trim();
    }
}