package com.routes.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.routes.client.FleetClient;
import com.routes.client.GeocodingClient;
import com.routes.client.PackageClient;
import com.routes.dto.client.fleets.FleetVehicleDTO;
import com.routes.dto.client.geocoding.BatchDistanceRequest;
import com.routes.dto.client.geocoding.BatchDistanceResponse;
import com.routes.dto.client.geocoding.DistanceResult;
import com.routes.dto.client.geocoding.LocationPair;
import com.routes.dto.client.packages.PackageDTO;
import com.routes.dto.client.packages.PackageStatusUpdateRequest;
import com.routes.dto.request.ShipmentRequestDTO;
import com.routes.dto.response.ShipmentLegDTO;
import com.routes.dto.response.ShipmentResponseDTO;
import com.routes.exception.AppException;
import com.routes.mapper.LocationMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Location;
import com.routes.model.entity.Route;
import com.routes.model.enums.LegStatus;
import com.routes.model.enums.RouteStatus;
import com.routes.repository.RouteRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for orchestrating the creation of shipments.
 * 
 * A shipment is a business operation that groups multiple packages
 * into a single route for delivery using one vehicle.
 * 
 * Flow:
 * 1. Validate packages are READY_FOR_PICKUP
 * 2. Validate vehicle has sufficient capacity
 * 3. Create a Route with one Leg per package
 * 4. Calculate distances and durations via ms-geocoding (batch)
 * 5. Update package status to IN_TRANSIT
 * 6. Return shipment details
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ShipmentService {

    private final RouteRepository routeRepository;
    private final LocationMapper locationMapper;
    private final LegService legService;

    private final PackageClient packageClient;
    private final FleetClient fleetClient;
    private final GeocodingClient geocodingClient;

    // ================================================================
    // PUBLIC METHODS
    // ================================================================

    /**
     * Creates a shipment from selected packages and a vehicle.
     * 
     * Step-by-step:
     * 1. Fetch package details from ms-packages
     * 2. Validate all packages are READY_FOR_PICKUP
     * 3. Fetch vehicle details from ms-fleets
     * 4. Validate vehicle capacity (total weight + volume)
     * 5. Create Route entity (status: PLANNED)
     * 6. Create one Leg per package (status: PENDING)
     * 7. Batch request to ms-geocoding for all distances
     * 8. Assign calculated distances to each Leg
     * 9. Calculate Route totals (total distance, total duration)
     * 10. Save Route with all Legs
     * 11. Update package status to IN_TRANSIT (ms-packages)
     * 12. Build and return ShipmentResponseDTO
     * 
     * @param request Contains package IDs, vehicle ID, and destination
     * @return ShipmentResponseDTO with route and tracking details
     * @throws AppException if validation fails (404, 400, 409)
     */
    @Transactional
    public ShipmentResponseDTO createShipment(ShipmentRequestDTO request) {
        log.info("Creating shipment with {} packages", request.packageIds().size());

        // ================================================================
        // STEP 1: Validate packages READY_FOR_PICKUP
        // ================================================================
        List<PackageDTO> packages = packageClient.getPackagesByIds(request.packageIds());
        validatePackagesReadyForPickup(packages);

        // ================================================================
        // STEP 2: Validate vehicle capacity (total weight + volume)
        // ================================================================
        FleetVehicleDTO vehicle = fleetClient.getVehicleById(request.vehicleId());
        validateVehicleCapacity(vehicle, packages);

        // ================================================================
        // STEP 3: Create Route
        // ================================================================
        Route route = new Route();
        route.setName("Shipment-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")));
        route.setDescription("Shipment for " + packages.size() + " packages");
        route.setStatus(RouteStatus.PLANNED);

        // ================================================================
        // STEP 4: Create one Leg per package and prepare batch request
        // ================================================================
        Location destination = locationMapper.toEntity(request.destination());
        List<Leg> legs = new ArrayList<>();
        List<LocationPair> locationPairs = new ArrayList<>();

        for (int i = 0; i < packages.size(); i++) {
            PackageDTO pkg = packages.get(i);
            Location origin = locationMapper.toEntity(pkg.origin()); // ← Store location

            // Create Leg without distance (will be set after batch response)
            Leg leg = new Leg();
            leg.setSequence(i + 1);
            leg.setRoute(route);
            leg.setVehicleId(request.vehicleId());
            leg.setPackageId(pkg.id());
            leg.setOrigin(origin);
            leg.setDestination(destination);
            leg.setStatus(LegStatus.PENDING);

            legs.add(leg);

            // Prepare batch request data
            locationPairs.add(new LocationPair(
                (long) i,  // Temporary leg ID (index in the list)
                locationMapper.toDto(origin),
                locationMapper.toDto(destination)
            ));
        }

        // ================================================================
        // STEP 5: Batch request to ms-geocoding (single call for all legs)
        // ================================================================
        BatchDistanceRequest batchRequest = new BatchDistanceRequest(locationPairs);
        BatchDistanceResponse batchResponse = geocodingClient.calculateDistances(batchRequest);

        // ================================================================
        // STEP 6: Assign calculated distances to each Leg
        // ================================================================
        for (DistanceResult result : batchResponse.results()) {
            int index = result.legId().intValue();
            Leg leg = legs.get(index);
            leg.setDistanceKm(result.distanceKm());
            leg.setDurationMinutes(result.durationMinutes());
        }

        // ================================================================
        // STEP 7: Calculate Route totals
        // ================================================================
        double totalDistance = legs.stream()
            .mapToDouble(Leg::getDistanceKm)
            .sum();
        int totalDuration = legs.stream()
            .mapToInt(Leg::getDurationMinutes)
            .sum();

        // dont use orm, is desactived, only save manual by service
        route.setLegs(legs);    
        route.setEstimatedDistanceKm(totalDistance);
        route.setEstimatedDurationMinutes(totalDuration);

        // ================================================================
        // STEP 8: Save Route n Legs
        // ================================================================
        Route savedRoute = routeRepository.save(route);
        log.info("Route created with id: {}", savedRoute.getId());

        legService.saveAllLegs(legs, savedRoute);

        // ================================================================
        // STEP 9: Update packages to IN_TRANSIT
        // ================================================================
        packageClient.updatePackageStatus(
            new PackageStatusUpdateRequest(
                request.packageIds(),
                "IN_TRANSIT"
            )
        );

        // ================================================================
        // STEP 10: Build and return response
        // ================================================================
        return buildShipmentResponse(savedRoute, vehicle, packages);
    }

    // ================================================================
    // PRIVATE VALIDATION METHODS
    // ================================================================

    /**
     * Validates that all packages exist and are ready for pickup.
     * 
     * Business Rule: Only packages with status READY_FOR_PICKUP can be shipped.
     * 
     * @param packages List of packages to validate
     * @throws AppException if any package is not READY_FOR_PICKUP
     */
    private void validatePackagesReadyForPickup(List<PackageDTO> packages) {
        if (packages.isEmpty()) {
            throw new AppException("No packages found", 404);
        }

        // Here you would validate that all packages have READY_FOR_PICKUP status.
        // For now, we assume they are valid.
        // TODO: Add status validation when PackageDTO includes status field
    }

    /**
     * Validates that the vehicle has sufficient capacity for all packages.
     * 
     * Business Rules:
     * - Total weight of all packages must not exceed vehicle maxWeightKg
     * - Total volume of all packages must not exceed vehicle maxVolumeCbm
     * 
     * @param vehicle The vehicle to validate
     * @param packages List of packages to check capacity for
     * @throws AppException if weight or volume exceeds vehicle capacity
     */
    private void validateVehicleCapacity(FleetVehicleDTO vehicle, List<PackageDTO> packages) {
        // Calculate total weight and volume
        double totalWeight = packages.stream()
            .mapToDouble(p -> p.totalWeightKg() != null ? p.totalWeightKg() : 0.0)
            .sum();

        double totalVolume = packages.stream()
            .mapToDouble(p -> p.totalVolumeCbm() != null ? p.totalVolumeCbm() : 0.0)
            .sum();

        // Validate weight capacity
        if (vehicle.maxWeightKg() != null && totalWeight > vehicle.maxWeightKg()) {
            throw new AppException(
                String.format("Total weight (%.2f kg) exceeds vehicle capacity (%.2f kg)", 
                    totalWeight, vehicle.maxWeightKg()), 
                400
            );
        }

        // Validate volume capacity
        if (vehicle.maxVolumeCbm() != null && totalVolume > vehicle.maxVolumeCbm()) {
            throw new AppException(
                String.format("Total volume (%.2f m³) exceeds vehicle capacity (%.2f m³)", 
                    totalVolume, vehicle.maxVolumeCbm()), 
                400
            );
        }
    }

    // ================================================================
    // RESPONSE BUILDER
    // ================================================================

    /**
     * Builds the ShipmentResponseDTO from the saved route and related data.
     * 
     * This method constructs a comprehensive response containing:
     * - Route details (id, name, status, totals)
     * - Vehicle information (id)
     * - Leg details for each package (id, packageId, tracking number, status,
     *   distance, duration, estimated arrival)
     * - Package weight and volume for reference
     * - Estimated arrival time and creation timestamp
     * 
     * @param route The saved Route entity
     * @param vehicle The FleetVehicleDTO used for the shipment
     * @param packages List of PackageDTOs included in the shipment
     * @return ShipmentResponseDTO with all shipment details
     */
    private ShipmentResponseDTO buildShipmentResponse(
        Route route, FleetVehicleDTO vehicle, List<PackageDTO> packages
    ) {
        // Step 1: Build LegDetailDTOs for each leg
        List<ShipmentLegDTO> legDetails = new ArrayList<>();
        
        for (int i = 0; i < route.getLegs().size() && i < packages.size(); i++) {

            Leg leg = route.getLegs().get(i);
            PackageDTO pkg = packages.get(i);

            // Calculate estimated arrival for this leg
            LocalDateTime estimatedArrival = route.getCreatedAt()
                .plusMinutes(leg.getDurationMinutes() != null ? leg.getDurationMinutes() : 0);

            ShipmentLegDTO legDetail = new ShipmentLegDTO(
                leg.getId(),                          // Leg ID
                leg.getSequence(),                    // Sequence in route
                leg.getStatus(),                      // Leg status (PENDING, IN_PROGRESS, etc.)
                leg.getDistanceKm(),                  // Distance in km
                leg.getDurationMinutes(),             // Duration in minutes
                leg.getVehicleId(),                   // Vehicle ID
                leg.getPackageId(),                   // Package ID
                pkg.totalWeightKg(),                  // Package weight (for reference)
                pkg.totalVolumeCbm(),                 // Package volume (for reference)
                leg.getOrigin(),                      // Origin location
                leg.getDestination(),                 // Destination location
                estimatedArrival
            );
            legDetails.add(legDetail);
        }

        // Step 2: Calculate overall estimated arrival (based on total duration)
        LocalDateTime estimatedArrivalTotal = route.getCreatedAt()
            .plusMinutes(route.getEstimatedDurationMinutes() != null 
                ? route.getEstimatedDurationMinutes() : 0);

        // Step 3: Build and return the response
        return new ShipmentResponseDTO(
            route.getId(),                           // Route ID
            route.getName(),                         // Route name
            route.getStatus(),                       // Route status (PLANNED, IN_PROGRESS, etc.)
            vehicle.id(),                     // Vehicle ID used
            route.getEstimatedDistanceKm(),          // Total distance in km
            route.getEstimatedDurationMinutes(),     // Total duration in minutes
            legDetails,                              // List of leg details
            estimatedArrivalTotal,                   // Estimated arrival time
            route.getCreatedAt()                     // Creation timestamp
        );
    }
}