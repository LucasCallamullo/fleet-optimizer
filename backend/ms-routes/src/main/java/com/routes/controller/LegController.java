package com.routes.controller;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.response.LegDetailDTO;
import com.routes.service.LegService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing Leg entities.
 * 
 * Provides CRUD operations for legs:
 * - GET /api/v1/legs/{id} - Retrieve a specific leg
 * - GET /api/v1/legs/route/{routeId} - Retrieve all legs for a route
 * - PUT /api/v1/legs/{id} - Update an existing leg
 * - DELETE /api/v1/legs/{id} - Delete a leg
 * 
 * Note: Legs are typically created as part of a Route (POST /api/v1/routes).
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/legs")
@RequiredArgsConstructor
public class LegController {

    private final LegService legService;

    // ================================================================
    // READ
    // ================================================================

    /**
     * Retrieves a leg by its ID.
     * 
     * @param id The ID of the leg to retrieve
     * @return The complete leg with locations (HTTP 200 OK)
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LegDetailDTO getLegById(@PathVariable Long id) {
        log.info("GET /api/v1/legs/{} - Fetching leg", id);
        return legService.getLegById(id);
    }

    /**
     * Retrieves all legs belonging to a specific route.
     * 
     * @param routeId The ID of the route
     * @return List of legs with their locations (HTTP 200 OK)
     */
    @GetMapping("/route/{routeId}")
    @ResponseStatus(HttpStatus.OK)
    public List<LegDetailDTO> getLegsByRouteId(@PathVariable Long routeId) {
        log.info("GET /api/v1/legs/route/{} - Fetching legs for route", routeId);
        return legService.getLegsByRouteId(routeId);
    }

    // ================================================================
    // UPDATE
    // ================================================================

    /**
     * Updates an existing leg.
     * 
     * @param id The ID of the leg to update
     * @param request The validated update data
     * @return The updated leg with all details (HTTP 200 OK)
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public LegDetailDTO updateLeg(
            @PathVariable Long id,
            @Valid @RequestBody LegRequestDTO request
    ) {
        log.info("PUT /api/v1/legs/{} - Updating leg", id);
        return legService.updateLeg(id, request);
    }

    // ================================================================
    // DELETE
    // ================================================================

    /**
     * Deletes a leg by its ID.
     * 
     * @param id The ID of the leg to delete
     * @throws com.routes.exception.AppException if leg is not found (404)
     * @ResponseStatus HTTP 204 No Content (successful deletion)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLeg(@PathVariable Long id) {
        log.info("DELETE /api/v1/legs/{} - Deleting leg", id);
        legService.deleteLeg(id);
    }
}