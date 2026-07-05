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

    @Override
    @Transactional
    public List<Leg> createLegEntities(List<LegRequestDTO> requests, Route route) {
        log.info("Creating {} legs for route: {}", requests.size(), route.getId());

        // Step 1: Create all Leg objects (transient)
        List<Leg> legs = new ArrayList<>();
        for (LegRequestDTO request : requests) {
            log.debug("Creating leg object with sequence: {} for route: {}", request.sequence(), route.getId());

            // Map DTO to Entity
            Leg leg = routeMapper.toLegEntity(request);
            leg.setRoute(route);
            leg.setStatus(LegStatus.PENDING);

            // add list
            legs.add(leg);
        }

        // Step 2: Bulk save all legs in a single query
        List<Leg> savedLegs = legRepository.saveAll(legs);
        log.info("Created {} legs successfully for route: {}", savedLegs.size(), route.getId());

        return savedLegs;
    }

    // ================================================================
    // READ
    // ================================================================

    @Override
    public LegDetailDTO getLegById(Long id) {
        log.debug("Fetching leg by id: {}", id);

        Leg leg = legRepository.findById(id)
            .orElseThrow(() -> new AppException("Leg not found with id: " + id, 404));

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
        Leg existingLeg = legRepository.findById(id)
            .orElseThrow(() -> new AppException("Leg not found with id: " + id, 404));

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

        Leg leg = legRepository.findById(id)
            .orElseThrow(() -> new AppException("Leg not found with id: " + id, 404));

        legRepository.delete(leg);
        log.info("Leg deleted successfully with id: {}", id);
    }
}