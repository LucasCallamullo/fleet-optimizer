package com.packages.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Request DTO for creating or updating a package.
 */
public record PackageRequestDTO(
    @NotBlank(message = "Tracking number is required")
    String trackingNumber,
    
    @Positive(message = "Total weight must be positive")
    Double totalWeightKg,
    
    @Positive(message = "Total volume must be positive")
    Double totalVolumeCbm,
    
    @NotNull(message = "Store ID is required")
    Long storeId
) {}