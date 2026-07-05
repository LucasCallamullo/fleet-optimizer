package com.routes.service;

import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.model.entity.Route;

/**
 * Service interface for managing Route entities.
 * Defines the business operations available for routes.
 */
public interface RouteService {
    
    /**
     * Creates a new route with its associated legs.
     * 
     * Business logic:
     * 1. Validates that the route has at least one leg
     * 2. Saves the route entity
     * 3. Creates and associates all legs to the route
     * 4. Calculates total distance and duration from legs
     * 5. Returns the complete route with all legs
     *
     * @param request The route creation request containing route data and legs
     * @return The created route with all details and associated legs
     * @throws com.routes.exception.AppException if route has no legs
     */
    RouteDetailDTO createRoute(RouteRequestDTO request);
    
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