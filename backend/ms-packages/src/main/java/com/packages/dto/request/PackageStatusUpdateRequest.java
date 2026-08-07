package com.packages.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request DTO for updating package statuses.
 */
public record PackageStatusUpdateRequest(
    
    /**
     * List of package IDs to update.
     * Must contain at least one ID.
     */
    @NotEmpty(message = "Package IDs list cannot be empty")
    List<Long> packageIds,
    
    /**
     * New status for the packages.
     * Must be a valid PackageStatus enum value.
     * Expected: "IN_TRANSIT", "DELIVERED", etc.
     */
    @NotNull(message = "Status is required")
    String status
) {}