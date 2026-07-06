package com.routes.service.impl;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.exception.AppException;
import com.routes.mapper.RouteMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Route;
import com.routes.model.enums.RouteStatus;
import com.routes.repository.RouteRepository;
import com.routes.service.GeocodingService;
import com.routes.service.LegService;
import com.routes.service.RouteService;
import com.routes.service.RouteValidationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RouteServiceImpl implements RouteService {

    private final RouteRepository routeRepository;
    private final RouteMapper routeMapper;
    private final LegService legService;
    private final RouteValidationService validationService;
    private final GeocodingService geocodingService;

    // ================================================================
    // CREATE
    // ================================================================

    @Override
    @Transactional
    public RouteDetailDTO createRoute(RouteRequestDTO request) {
        log.info("Creating new route: {}", request.name());

        // Step 1: Validate request
        this.validateRouteRequest(request);

        // Step 2: Validate vehicles and packages (external MS)
        validationService.validateLegs(request.legs());

        // Step 3: Map DTO to Entity
        Route route = routeMapper.toEntity(request);
        route.setStatus(RouteStatus.PLANNED);

        // Step 4: Save Route (generates ID)
        Route savedRoute = routeRepository.save(route);
        log.debug("Route saved with id: {}", savedRoute.getId());

        // Step 5: Process legs (create, calculate, save, associate)
        List<Leg> savedLegs = processLegs(request.legs(), savedRoute);

        // Step 6: Update route with legs
        savedRoute.getLegs().addAll(savedLegs);

        log.info("Route created successfully with id: {}", savedRoute.getId());
        return routeMapper.toDetailDto(savedRoute);
    }

    // ================================================================
    // UPDATE
    // ================================================================

    @Override
    @Transactional
    public RouteDetailDTO updateRoute(Long id, RouteRequestDTO request) {
        log.info("Updating route with id: {}", id);
        // Step 1: Validate request
        this.validateRouteRequest(request);

        // Step 2: Find existing Route
        Route existingRoute = this.getRouteByIdWithLegs(id);

        // Step 3: Update basic fields
        existingRoute.setName(request.name());
        existingRoute.setDescription(request.description());

        // Step 3: Clear old legs
        existingRoute.getLegs().clear();

        // Step 4: Save Route (updated)
        Route updatedRoute = routeRepository.save(existingRoute);
        log.debug("Route updated with id: {}", updatedRoute.getId());

        // Step 5: Process legs (create, calculate, save, associate)
        List<Leg> savedLegs = processLegs(request.legs(), updatedRoute);

        // Step 6: Update route with legs
        updatedRoute.getLegs().addAll(savedLegs);

        log.info("Route updated successfully with id: {}", updatedRoute.getId());
        return routeMapper.toDetailDto(updatedRoute);
    }

    // ================================================================
    // PRIVATE HELPER METHODS
    // ================================================================

    /**
     * Validates that the route request has at least one leg.
     *
     * @param request The route request
     * @throws AppException if no legs are provided
     */
    private void validateRouteRequest(RouteRequestDTO request) {
        if (request.legs() == null || request.legs().isEmpty()) {
            throw new AppException("Route must have at least one leg", 400);
        }
    }

    /**
     * Processes legs for a route:
     * 1. Creates Leg objects in memory
     * 2. Calculates distances and durations (OSRM)
     * 3. Calculates totals
     * 4. Saves legs with route association
     *
     * @param legRequests List of leg requests
     * @param route The route to associate legs with
     * @return List of persisted legs
     */
    private List<Leg> processLegs(List<LegRequestDTO> legRequests, Route route) {
        // Step 1: Create Leg objects in memory
        List<Leg> legs = legService.createLegsInMemory(legRequests);

        // Step 2: Calculate distances and durations (OSRM)
        List<Leg> calculatedLegs = geocodingService.calculateLegDistances(legs);

        // Step 3: Update route totals
        this.updateRouteTotals(route, calculatedLegs);

        // Step 4: Bulk save legs with route association
        List<Leg> savedLegs = legService.saveAllLegs(calculatedLegs, route);
        log.debug("Saved {} legs for route: {}", savedLegs.size(), route.getId());

        return savedLegs;
    }

    /**
     * Calculates and sets estimated distance and duration on the route.
     *
     * @param route The route to update
     * @param legs The calculated legs
     */
    private void updateRouteTotals(Route route, List<Leg> legs) {
        double totalDistance = legs.stream()
            .mapToDouble(leg -> leg.getDistanceKm() != null ? leg.getDistanceKm() : 0.0)
            .sum();

        int totalDuration = legs.stream()
            .mapToInt(leg -> leg.getDurationMinutes() != null ? leg.getDurationMinutes() : 0)
            .sum();

        route.setEstimatedDistanceKm(totalDistance);
        route.setEstimatedDurationMinutes(totalDuration);

        log.debug("Route totals - distance: {}km, duration: {}min", totalDistance, totalDuration);
    }

    // ================================================================
    // DELETE
    // ================================================================

    @Override
    @Transactional
    public void deleteRoute(Long id) {
        log.info("Deleting route with id: {}", id);

        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new AppException("Route not found with id: " + id, 404));

        routeRepository.delete(route);
        log.info("Route deleted successfully with id: {}", id);
    }

    // ================================================================
    // READ
    // ================================================================

    @Override
    public RouteDetailDTO getRouteById(Long id) {
        Route route = this.getRouteByIdWithLegs(id);
        return routeMapper.toDetailDto(route);
    }

    // ================================================================
    // READ ENTITIES
    // ================================================================

    @Override
    public Route getRouteByIdWithLegs(Long id) {
        log.debug("Fetching route by id with legs: {}", id);

        // Trae la Route con todos sus Legs en una sola query (JOIN FETCH)
        Route route = routeRepository.findByIdWithLegs(id)
            .orElseThrow(() -> new AppException("Route not found with id: " + id, 404));

        log.debug("Route found: {} with {} legs", route.getName(), route.getLegs().size());
        return route;
    }
}