package com.routes.dto.external;

import java.util.List;

/**
 * Request DTO for updating package statuses in ms-packages.
 */
public record PackageStatusUpdateRequest(
    List<Long> packageIds,
    String status
) {}