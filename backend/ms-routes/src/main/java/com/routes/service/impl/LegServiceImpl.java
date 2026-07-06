package com.routes.service.impl;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.response.LegDetailDTO;
import com.routes.exception.AppException;
import com.routes.mapper.RouteMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Route;
import com.routes.model.enums.LegStatus;
import com.routes.repository.LegRepository;
import com.routes.service.LegService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LegServiceImpl implements LegService {

    private final LegRepository legRepository;
    private final RouteMapper routeMapper;

    // ================================================================
    // CREATE
    // ================================================================

    /**
     * Creates Leg entities from requests (transient, not persisted).
     */
    @Override
    public List<Leg> createLegsInMemory(List<LegRequestDTO> requests) {
        log.debug("Creating {} legs in memory", requests.size());

        List<Leg> legs = new ArrayList<>();
        for (LegRequestDTO request : requests) {
            // Map DTO to Entity
            Leg leg = routeMapper.toLegEntity(request);
            leg.setStatus(LegStatus.PENDING);

            // NO seteo route aún (solo en memoria)
            // leg.setRoute(route);
            legs.add(leg);
        }

        return legs;
    }

    /**
     * Bulk saves legs to database.
     * 
     * Business rules:
     * - Legs CANNOT exist without a Route (route must not be null)
     * - Legs list CANNOT be empty (at least one leg required)
     * 
     * @param legs List of legs to persist
     * @param route The route to associate with each leg
     * @return List of persisted Leg entities
     * @throws AppException if route is null or legs list is empty
     */
    @Override
    @Transactional
    public List<Leg> saveAllLegs(List<Leg> legs, Route route) {
        
        // Step 1: Validate that route is not null
        if (route == null) {
            log.error("Attempted to save legs without a route");
            throw new AppException("Cannot save legs without an associated route", 400);
        }

        // Step 2: Validate that legs list is not empty
        if (legs == null || legs.isEmpty()) {
            log.error("Attempted to save empty legs list for route: {}", route.getId());
            throw new AppException("Cannot save legs: at least one leg is required", 400);
        }

        // Step 3: Associate Route to each Leg (in memory)
        for (Leg leg : legs) {
            leg.setRoute(route);
        }
        log.debug("Associated {} legs to route: {}", legs.size(), route.getId());

        // Step 4: Bulk save all legs
        List<Leg> savedLegs = legRepository.saveAll(legs);
        log.info("Saved {} legs successfully for route: {}", savedLegs.size(), route.getId());

        return savedLegs;
    }

    // ================================================================
    // READ ENTITIES
    // ================================================================

    @Override
    public Leg getLegEntityById(Long id) {
        log.debug("Fetching leg entity by id: {}", id);

        return legRepository.findById(id)
            .orElseThrow(() -> new AppException("Leg not found with id: " + id, 404));
    }

    // ================================================================
    // READ DTOs
    // ================================================================

    @Override
    public LegDetailDTO getLegById(Long id) {
        log.debug("Fetching leg by id: {}", id);

        Leg leg = this.getLegEntityById(id);
        return routeMapper.toLegDetailDto(leg);
    }

    @Override
    public List<LegDetailDTO> getLegsByRouteId(Long routeId) {
        log.debug("Fetching legs for route: {}", routeId);

        List<Leg> legs = legRepository.findByRouteId(routeId);
        return legs.stream()
            .map(routeMapper::toLegDetailDto)
            .toList();
    }

    // ================================================================
    // UPDATE
    // ================================================================

    @Override
    @Transactional
    public LegDetailDTO updateLeg(Long id, LegRequestDTO request) {
        log.info("Updating leg with id: {}", id);

        // Step 1: Find existing Leg
        Leg existingLeg = this.getLegEntityById(id);

        // Step 2: Update fields
        existingLeg.setSequence(request.sequence());
        existingLeg.setVehicleId(request.vehicleId());
        existingLeg.setPackageId(request.packageId());
        existingLeg.setOrigin(routeMapper.toLocationEntity(request.origin()));
        existingLeg.setDestination(routeMapper.toLocationEntity(request.destination()));

        // Step 3: Save
        Leg updatedLeg = legRepository.save(existingLeg);
        log.info("Leg updated successfully with id: {}", updatedLeg.getId());

        return routeMapper.toLegDetailDto(updatedLeg);
    }

    // ================================================================
    // DELETE
    // ================================================================

    @Override
    @Transactional
    public void deleteLeg(Long id) {
        log.info("Deleting leg with id: {}", id);

        Leg leg = this.getLegEntityById(id);

        legRepository.delete(leg);
        log.info("Leg deleted successfully with id: {}", id);
    }
}