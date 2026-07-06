package com.routes.service;

import com.routes.client.FleetClient;
import com.routes.client.PackageClient;
import com.routes.dto.external.FleetVehicleDTO;
import com.routes.dto.external.PackageDTO;
import com.routes.dto.request.LegRequestDTO;
import com.routes.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RouteValidationService {

    private final FleetClient fleetClient;
    private final PackageClient packageClient;

    /**
     * Validates all vehicles and packages for a route.
     * 
     * @param legs List of leg requests
     * @throws AppException if any validation fails
     */
    public void validateLegs(List<LegRequestDTO> legs) {
        log.info("Validating {} legs", legs.size());

        // Step 1: Extract IDs
        List<Long> vehicleIds = legs.stream()
            .map(LegRequestDTO::vehicleId)
            .distinct()
            .toList();

        List<Long> packageIds = legs.stream()
            .map(LegRequestDTO::packageId)
            .distinct()
            .toList();

        log.debug("Vehicle IDs: {}, Package IDs: {}", vehicleIds, packageIds);

        // Step 2: Fetch vehicles from ms-fleets (Feign handles errors)
        List<FleetVehicleDTO> vehicles = fleetClient.getVehiclesByIds(vehicleIds);
        log.debug("Fetched {} vehicles from ms-fleets", vehicles.size());
        
        // Step 3: Fetch packages from ms-packages (Feign handles errors)
        List<PackageDTO> packages = packageClient.getPackagesByIds(packageIds);
        log.debug("Fetched {} packages from ms-packages", packages.size());

        // Step 4: Validate each leg
        for (LegRequestDTO leg : legs) {
            validateLeg(leg, vehicles, packages);
        }

        log.info("All legs validated successfully");
    }

    private void validateLeg(LegRequestDTO leg, List<FleetVehicleDTO> vehicles, List<PackageDTO> packages) {
        // Find the vehicle for this leg
        FleetVehicleDTO vehicle = vehicles.stream()
            .filter(v -> v.id().equals(leg.vehicleId()))
            .findFirst()
            .orElseThrow(() -> new AppException("Vehicle not found: " + leg.vehicleId(), 404));

        // Find the package for this leg
        PackageDTO pkg = packages.stream()
            .filter(p -> p.id().equals(leg.packageId()))
            .findFirst()
            .orElseThrow(() -> new AppException("Package not found: " + leg.packageId(), 404));

        log.debug("Validating leg {}: vehicle={}, package={}", 
            leg.sequence(), vehicle.id(), pkg.id());

        // Validate weight capacity
        if (vehicle.maxWeightKg() != null && pkg.totalWeightKg() != null) {
            if (pkg.totalWeightKg() > vehicle.maxWeightKg()) {
                throw new AppException(
                    String.format("Package weight (%.2f kg) exceeds vehicle capacity (%.2f kg) for leg %d",
                        pkg.totalWeightKg(), vehicle.maxWeightKg(), leg.sequence()),
                    400
                );
            }
        }

        // Validate volume capacity
        if (vehicle.maxVolumeCbm() != null && pkg.totalVolumeCbm() != null) {
            if (pkg.totalVolumeCbm() > vehicle.maxVolumeCbm()) {
                throw new AppException(
                    String.format("Package volume (%.2f m³) exceeds vehicle capacity (%.2f m³) for leg %d",
                        pkg.totalVolumeCbm(), vehicle.maxVolumeCbm(), leg.sequence()),
                    400
                );
            }
        }

        log.debug("Leg {} validation passed", leg.sequence());
    }
}