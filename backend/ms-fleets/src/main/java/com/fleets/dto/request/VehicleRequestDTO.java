package com.fleets.dto.request;

import com.fleets.model.VehicleStatus;
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

    // ================================================================
    // PHYSICAL CAPACITIES - Optional fields
    // ================================================================

    /**
     * Maximum cargo capacity in kilograms.
     * Optional - can be null if not defined.
     */
    @Positive(message = "Max weight must be positive")
    private Double maxWeightKg;

    /**
     * Maximum volumetric capacity in cubic meters.
     * Optional - can be null if not defined.
     */
    @Positive(message = "Max volume must be positive")
    private Double maxVolumeCbm;

    // ================================================================
    // EFFICIENCY AND COSTS - Optional fields
    // ================================================================

    /**
     * Fuel consumption in liters per kilometer.
     * Optional - can be null if not defined.
     */
    @Positive(message = "Fuel consumption must be positive")
    private Double fuelConsumptionPerKm;

    /**
     * Operational cost per kilometer.
     * Optional - can be null if not defined.
     */
    @Positive(message = "Cost per km must be positive")
    private Double costPerKm;

    /**
     * Sale price per kilometer.
     * Optional - can be null if not defined.
     */
    @Positive(message = "Price per km must be positive")
    private Double pricePerKm;

    // ================================================================
    // VEHICLE STATUS - Optional field
    // ================================================================

    /**
     * Current vehicle status.
     * If not provided, defaults to AVAILABLE in the entity.
     */
    private VehicleStatus status;
}