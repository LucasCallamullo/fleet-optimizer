package com.routes.dto.external;

public record FleetVehicleDTO(
    Long id,
    String licensePlate,
    Double maxWeightKg,
    Double maxVolumeCbm,
    String status
) {}