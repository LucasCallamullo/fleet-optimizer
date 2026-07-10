package com.packages.dto.response;

import com.packages.model.enums.PackageStatus;

import java.time.LocalDateTime;

/**
 * Detailed package response DTO with full store information.
 * Includes location details for the store (origin).
 */
public record PackageDetailDTO(
    Long id,
    String trackingNumber,
    Double totalWeightKg,
    Double totalVolumeCbm,
    PackageStatus status,
    String ownerId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    StoreResponseDTO store
) {}