package com.packages.dto.external;

/**
 * DTO for inter-service communication with ms-routes.
 * 
 * This contains all information needed by ms-routes for:
 * - Vehicle capacity validation (weight, volume)
 * - Route planning (origin location from store)
 * 
 * The destination is NOT included here because it's provided by
 * the client when creating a route in ms-routes.
 */
public record PackageDTO(
    Long id,
    Double totalWeightKg,
    Double totalVolumeCbm,
    LocationDTO origin  // ← Store location (package origin)
) {}