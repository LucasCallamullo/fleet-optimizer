package com.fleets.dto.response;

/**
 * DTO for Vehicle response.
 * Excludes the list of vehicles from Category to avoid circular reference.
 */
public record VehicleDetailDTO(
    Long id,
    String licensePlate,
    Integer year,
    CategoryResponseDTO category
) {

}