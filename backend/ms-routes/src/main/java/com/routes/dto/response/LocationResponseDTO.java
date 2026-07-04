package com.routes.dto.response;

/**
 * DTO for location response.
 * Mirror of Location embeddable but as a standalone DTO.
 */
public record LocationResponseDTO(
    
    String street,
    
    String streetNumber,
    
    String city,
    
    String state,
    
    String country,
    
    String postalCode,
    
    Double latitude,
    
    Double longitude
    
) {}