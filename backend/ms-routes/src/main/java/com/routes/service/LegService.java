package com.routes.service;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.response.LegDetailDTO;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Route;

import java.util.List;

/**
 * Service interface for managing Leg entities.
 * Defines the business operations available for legs.
 */
public interface LegService {
    
    /**
     * Creates multiple leg entities for a route and persists them in bulk.
     *
     * @param requests List of leg creation requests
     * @param route The Route entity to associate legs with
     * @return List of persisted Leg entities
     */
    List<Leg> createLegEntities(List<LegRequestDTO> requests, Route route);
    
    /**
     * Retrieves a leg by its ID.
     *
     * @param id The ID of the leg to retrieve
     * @return The complete leg with origin and destination locations
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    LegDetailDTO getLegById(Long id);
    
    /**
     * Retrieves all legs belonging to a specific route.
     *
     * @param routeId The ID of the route
     * @return List of legs with their locations, ordered by sequence
     */
    List<LegDetailDTO> getLegsByRouteId(Long routeId);
    
    /**
     * Updates an existing leg.
     * 
     * Business logic:
     * 1. Finds the existing leg by ID
     * 2. Updates all fields (sequence, vehicle, package, origin, destination)
     * 3. Persists the updated leg
     *
     * @param id The ID of the leg to update
     * @param request The updated leg data
     * @return The updated leg with all details
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    LegDetailDTO updateLeg(Long id, LegRequestDTO request);
    
    /**
     * Deletes a leg by its ID.
     *
     * @param id The ID of the leg to delete
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    void deleteLeg(Long id);
}