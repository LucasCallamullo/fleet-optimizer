package com.routes.dto.client.packages;

import com.routes.dto.client.common.LocationDTO;

public record PackageDTO(
    Long id,
    Double totalWeightKg,
    Double totalVolumeCbm,
    LocationDTO origin
) {}