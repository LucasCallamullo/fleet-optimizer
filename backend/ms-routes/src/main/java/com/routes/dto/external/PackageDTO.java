package com.routes.dto.external;

public record PackageDTO(
    Long id,
    String trackingNumber,
    Double totalWeightKg,
    Double totalVolumeCbm
) {}