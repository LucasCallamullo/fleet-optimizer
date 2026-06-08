package com.fleets.dto.response;

import com.fleets.model.Vehicle;

/**
 * DTO for Vehicle response.
 * Excludes the list of vehicles from Category to avoid circular reference.
 */
public record VehicleResponseDTO(
    Long id,
    String licensePlate,
    Integer year,
    CategoryResponseDTO category
) {

    /**
     * Converts a Vehicle entity to VehicleResponseDTO
     * @param vehicle the vehicle entity
     * @return the response DTO, or null if vehicle is null
     */
    public static VehicleResponseDTO fromEntity(Vehicle vehicle) {
        if (vehicle == null) return null;
        
        CategoryResponseDTO categoryDTO = CategoryResponseDTO.fromEntity(vehicle.getCategory());
        
        return new VehicleResponseDTO(
            vehicle.getId(),
            vehicle.getLicensePlate(),
            vehicle.getYear(),
            categoryDTO
        );
    }
}