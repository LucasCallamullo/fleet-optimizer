package com.routes.controller;

import com.routes.dto.request.ShipmentRequestDTO;
import com.routes.dto.response.ShipmentResponseDTO;
import com.routes.exception.AppException;
import com.routes.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Shipment operations.
 * 
 * A shipment is a business operation that groups multiple packages
 * into a single route for delivery using one vehicle.
 * 
 * This controller handles the orchestration of package delivery,
 * coordinating with other microservices (ms-packages, ms-fleets, ms-geocoding).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // ================================================================
    // CREATE SHIPMENT
    // ================================================================

    /**
     * Creates a new shipment from selected packages and a vehicle.
     * 
     * Endpoint: POST /api/v1/shipments
     * 
     * This operation orchestrates the entire delivery creation process:
     * 1. Validates packages exist and are READY_FOR_PICKUP (ms-packages)
     * 2. Validates vehicle exists and has capacity (ms-fleets)
     * 3. Creates a Route with one Leg per package
     * 4. Calculates distances and durations via OSRM (ms-geocoding)
     * 5. Updates package status to IN_TRANSIT
     * 6. Returns shipment details with tracking information
     * 
     * Security: Requires authenticated user (any role)
     * 
     * @param request Contains package IDs, vehicle ID, and destination location
     * @return ShipmentResponseDTO with route and tracking details
     * @throws AppException if validation fails (404, 400, 409)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public ShipmentResponseDTO createShipment(@Valid @RequestBody ShipmentRequestDTO request) {
        log.info("POST /api/v1/shipments - Creating shipment with {} packages", request.packageIds().size());
        ShipmentResponseDTO response = shipmentService.createShipment(request);
        log.info("Shipment created successfully - routeId: {}, {} legs", 
            response.routeId(), response.legs().size());
        return response;
    }
}