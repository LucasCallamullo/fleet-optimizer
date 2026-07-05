package com.routes.service.impl;

import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.exception.AppException;
import com.routes.mapper.RouteMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Route;
import com.routes.model.enums.RouteStatus;
import com.routes.repository.RouteRepository;
import com.routes.service.LegService;
import com.routes.service.RouteService;

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

    // ================================================================
    // CREATE
    // ================================================================

    @Override
    @Transactional
    public RouteDetailDTO createRoute(RouteRequestDTO request) {

        // Step 1: Validate request
        if (request.legs() == null || request.legs().isEmpty()) {
            throw new AppException("Route must have at least one leg", 400);
        }

        // Step 2: Map DTO to Entity
        Route route = routeMapper.toEntity(request);
        route.setStatus(RouteStatus.PLANNED);

        // Step 3: Save Route first (to get ID)
        Route savedRoute = routeRepository.save(route);
        log.debug("Route saved with id: {}", savedRoute.getId());

        // Step 4: Create Legs and associate with Route
        List<Leg> createdLegs = legService.createLegEntities(request.legs(), savedRoute);

        // Step 5: Complete data on object
        savedRoute.getLegs().addAll(createdLegs);

        // Step 6: Calculate totals from legs (USAR createdLegs, NO savedRoute.getLegs())
        double totalDistance = createdLegs.stream()
            .mapToDouble(leg -> leg.getDistanceKm() != null ? leg.getDistanceKm() : 0.0)
            .sum();
        int totalDuration = createdLegs.stream()
            .mapToInt(leg -> leg.getDurationMinutes() != null ? leg.getDurationMinutes() : 0)
            .sum();

        savedRoute.setEstimatedDistanceKm(totalDistance);
        savedRoute.setEstimatedDurationMinutes(totalDuration);

        // Step 7: Save updated route
        Route finalRoute = routeRepository.save(savedRoute);

        log.info("Route created successfully with id: {}", finalRoute.getId());
        return routeMapper.toDetailDto(finalRoute);
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
    // UPDATE
    // ================================================================

    @Override
    @Transactional
    public RouteDetailDTO updateRoute(Long id, RouteRequestDTO request) {
        log.info("Updating route with id: {}", id);

        // Step 1: Find existing Route
        Route existingRoute = routeRepository.findById(id)
            .orElseThrow(() -> new AppException("Route not found with id: " + id, 404));

        // Step 2: Update basic fields
        existingRoute.setName(request.name());
        existingRoute.setDescription(request.description());

        // Step 3: Delete old legs and create new ones
        // (Simple approach: remove all and add new)
        existingRoute.getLegs().clear();

        // Step 4: Create new legs
        legService.createLegEntities(request.legs(), existingRoute);

        // Step 5: Recalculate totals
        double totalDistance = existingRoute.getLegs().stream()
            .mapToDouble(leg -> leg.getDistanceKm() != null ? leg.getDistanceKm() : 0.0)
            .sum();
        int totalDuration = existingRoute.getLegs().stream()
            .mapToInt(leg -> leg.getDurationMinutes() != null ? leg.getDurationMinutes() : 0)
            .sum();

        existingRoute.setEstimatedDistanceKm(totalDistance);
        existingRoute.setEstimatedDurationMinutes(totalDuration);

        Route updatedRoute = routeRepository.save(existingRoute);
        log.info("Route updated successfully with id: {}", updatedRoute.getId());

        return routeMapper.toDetailDto(updatedRoute);
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