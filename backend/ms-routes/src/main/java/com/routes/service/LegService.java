package com.routes.service;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.response.LegDetailDTO;
import com.routes.exception.AppException;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Route;

import java.util.List;

/**
 * Service interface for managing Leg entities.
 */
public interface LegService {

    // ================================================================
    // CREATE (In Memory)
    // ================================================================

    /**
     * Creates Leg entities from requests (transient, not persisted).
     * Use this when you want to create legs in memory without saving.
     * 
     * @param requests List of leg creation requests
     * @return List of transient Leg entities (not yet persisted)
     */
    List<Leg> createLegsInMemory(List<LegRequestDTO> requests);

    /**
     * Bulk saves legs to database and associates them with a route.
     * 
     * @param legs List of legs to persist
     * @param route The route to associate with each leg
     * @return List of persisted Leg entities
     * @throws AppException if route is null or legs list is empty
     */
    List<Leg> saveAllLegs(List<Leg> legs, Route route);

    // ================================================================
    // READ
    // ================================================================

    /**
     * Retrieves a leg entity by its ID.
     * Throws AppException if not found.
     * 
     * @param id The ID of the leg to retrieve
     * @return The Leg entity
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    Leg getLegEntityById(Long id);

    /**
     * Retrieves a leg by its ID as a DTO.
     * 
     * @param id The ID of the leg to retrieve
     * @return The complete leg with locations
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    LegDetailDTO getLegById(Long id);

    /**
     * Retrieves all legs belonging to a specific route as DTOs.
     * 
     * @param routeId The ID of the route
     * @return List of legs with their locations, ordered by sequence
     */
    List<LegDetailDTO> getLegsByRouteId(Long routeId);

    // ================================================================
    // UPDATE
    // ================================================================

    /**
     * Updates an existing leg.
     * 
     * @param id The ID of the leg to update
     * @param request The updated leg data
     * @return The updated leg with all details
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    LegDetailDTO updateLeg(Long id, LegRequestDTO request);

    // ================================================================
    // DELETE
    // ================================================================

    /**
     * Deletes a leg by its ID.
     * 
     * @param id The ID of the leg to delete
     * @throws com.routes.exception.AppException if leg is not found (404)
     */
    void deleteLeg(Long id);
}