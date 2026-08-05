package com.routes.dto.client.packages;

import java.util.List;

/**
 * Request DTO for updating package statuses in ms-packages.
 */
public record PackageStatusUpdateRequest(
    List<Long> packageIds,
    String status
) {}