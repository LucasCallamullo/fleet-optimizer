package com.packages.dto.response;

import com.packages.model.enums.PackageStatus;

/**
 * Basic package response DTO for list endpoints.
 * Includes store ID reference but not full store details.
 */
public record PackageResponseDTO(
    Long id,
    String trackingNumber,
    Double totalWeightKg,
    Double totalVolumeCbm,
    PackageStatus status,
    Long storeId,
    String ownerId
) {}