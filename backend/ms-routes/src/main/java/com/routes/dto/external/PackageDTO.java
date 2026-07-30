package com.routes.dto.external;

import com.routes.dto.request.LocationRequestDTO;

public record PackageDTO(
    Long id,
    Double totalWeightKg,
    Double totalVolumeCbm,
    LocationRequestDTO origin
) {}