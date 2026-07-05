package com.routes.controller;

import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.service.RouteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing Route entities.
 * 
 * Provides CRUD operations for routes:
 * - POST /api/v1/routes - Create a new route with legs
 * - GET /api/v1/routes/{id} - Retrieve a specific route
 * - PUT /api/v1/routes/{id} - Update an existing route
 * - DELETE /api/v1/routes/{id} - Delete a route
 * 
 * All endpoints return JSON responses and use DTOs for data transfer.
 * Validation is applied to incoming requests using @Valid.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * Creates a new route with its associated legs.
     * 
     * Endpoint: POST /api/v1/routes
     * 
     * Request body: RouteRequestDTO containing route name, description,
     * and a list of LegRequestDTO with origin/destination locations.
     * 
     * Business logic:
     * 1. Validates the request using @Valid annotation
     * 2. Delegates to RouteService for creation
     * 3. Returns the complete route with all legs and calculated totals
     * 
     * @param request The validated route creation request
     * @return The created route with full details (HTTP 201 Created)
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteDetailDTO createRoute(@Valid @RequestBody RouteRequestDTO request) {
        log.info("POST /api/v1/routes - Creating route with {} legs", request.legs().size());
        return routeService.createRoute(request);
    }

    /**
     * Retrieves a route by its ID.
     * 
     * Endpoint: GET /api/v1/routes/{id}
     * 
     * The response includes all route details and its associated legs,
     * with origin and destination locations for each leg.
     * 
     * @param id The ID of the route to retrieve
     * @return The complete route with all legs (HTTP 200 OK)
     * @throws com.routes.exception.AppException if route is not found (404)
     */
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RouteDetailDTO getRouteById(@PathVariable Long id) {
        log.info("GET /api/v1/routes/{} - Fetching route", id);
        return routeService.getRouteById(id);
    }

    /**
     * Updates an existing route.
     * 
     * Endpoint: PUT /api/v1/routes/{id}
     * 
     * Request body: RouteRequestDTO containing updated route data.
     * This operation replaces all existing legs with the new ones
     * provided in the request and recalculates totals.
     * 
     * @param id The ID of the route to update
     * @param request The validated update data
     * @return The updated route with all legs (HTTP 200 OK)
     * @throws com.routes.exception.AppException if route is not found (404)
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public RouteDetailDTO updateRoute(
            @PathVariable Long id,
            @Valid @RequestBody RouteRequestDTO request
    ) {
        log.info("PUT /api/v1/routes/{} - Updating route", id);
        return routeService.updateRoute(id, request);
    }

    /**
     * Deletes a route by its ID.
     * 
     * Endpoint: DELETE /api/v1/routes/{id}
     * 
     * This operation cascades to delete all associated legs.
     * No response body is returned on successful deletion.
     * 
     * @param id The ID of the route to delete
     * @throws com.routes.exception.AppException if route is not found (404)
     * @ResponseStatus HTTP 204 No Content (successful deletion)
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRoute(@PathVariable Long id) {
        log.info("DELETE /api/v1/routes/{} - Deleting route", id);
        routeService.deleteRoute(id);
    }
}