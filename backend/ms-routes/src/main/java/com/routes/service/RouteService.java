package com.routes.service;

import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.exception.AppException;
import com.routes.model.entity.Route;

/**
 * Service interface for managing Route entities.
 * Defines the business operations available for routes.
 */
public interface RouteService {
    
    /**
     * Saves a Route entity to the database.
     * 
     * This method is used internally by other services (e.g., ShipmentService)
     * to persist route data. It provides a single point of control for
     * route persistence, allowing for:
     * - Pre-save validation
     * - Audit trail (createdAt, updatedAt)
     * - Business rule enforcement
     * 
     * When to use this method:
     * - Creating a new Route through ShipmentService
     * - Persisting route data after business logic validation
     * - Internal operations that require direct route persistence
     * 
     * Business Rules:
     * - The route must have a non-null name and status
     * - Legs associated with the route will be saved separately
     * - Status must be PLANNED when creating a new route
     * - CreatedAt and UpdatedAt timestamps are auto-managed by Hibernate
     * 
     * Note: This method does NOT cascade-save legs. Legs must be saved
     * separately via LegService to maintain explicit control.
     * 
     * @param route The Route entity to persist
     * @return The persisted Route entity with generated ID and timestamps
     * @throws AppException if route data is invalid (e.g., missing required fields)
     */
    Route save(Route route);

    /**
     * Retrieves a route by its ID with all associated legs.
     *
     * @param id The ID of the route to retrieve
     * @return The complete route with all legs
     * @throws com.routes.exception.AppException if route is not found (404)
     */
    RouteDetailDTO getRouteById(Long id);
    
    /**
     * Updates an existing route.
     * 
     * Business logic:
     * 1. Finds the existing route by ID
     * 2. Updates basic route fields (name, description)
     * 3. Replaces all legs with the new ones from the request
     * 4. Recalculates total distance and duration
     *
     * @param id The ID of the route to update
     * @param request The updated route data
     * @return The updated route with all legs
     * @throws com.routes.exception.AppException if route is not found (404)
     */
    RouteDetailDTO updateRoute(Long id, RouteRequestDTO request);
    
    /**
     * Deletes a route by its ID.
     * 
     * Business logic:
     * 1. Finds the existing route by ID
     * 2. Deletes the route (cascade deletes associated legs)
     *
     * @param id The ID of the route to delete
     * @throws com.routes.exception.AppException if route is not found (404)
     */
    void deleteRoute(Long id);


    /**
     * Retrieves a route by its ID with all associated legs eagerly loaded.
     * Uses JOIN FETCH to load legs in a single query.
     * 
     * @param id The ID of the route to retrieve
     * @return The complete route with all legs
     * @throws com.routes.exception.AppException if route is not found (404)
     */
    Route getRouteByIdWithLegs(Long id);
}