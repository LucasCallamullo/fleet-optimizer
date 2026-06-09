package com.fleets.dto.request;

import com.fleets.model.Vehicle;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating and updating Vehicle entities.
 * Contains validation annotations to ensure data integrity before reaching the service layer.
 */
@Data
public class VehicleRequestDTO {
    
    /**
     * Vehicle license plate number.
     * Must be unique across all vehicles.
     * Format: 6-10 characters, alphanumeric.
     */
    @NotBlank(message = "License plate is required")
    @Size(min = 6, max = 10, message = "License plate must be between 6 and 10 characters")
    @Pattern(regexp = "^[A-Z0-9]+$", message = "License plate must contain only uppercase letters and numbers")
    private String licensePlate;
    
    /**
     * Vehicle manufacturing year.
     * Must be between 1900 and current year.
     */
    @NotNull(message = "Year is required")
    @Min(value = 1900, message = "Year must be after 1900")
    @Max(value = 2026, message = "Year cannot be in the future")
    private Integer year;
    
    /**
     * Optional category ID for the vehicle.
     * Can be null (vehicle without category).
     */
    @Positive(message = "Category ID must be positive")
    private Long categoryId;
    
    /**
     * Converts this DTO to a Vehicle entity.
     * Note: Category needs to be fetched separately in the service layer.
     * 
     * @return a new Vehicle entity with basic fields populated
     */
    public Vehicle toEntity() {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(this.licensePlate);
        vehicle.setYear(this.year);
        // Category is set separately in service to avoid lazy loading issues
        return vehicle;
    }
}