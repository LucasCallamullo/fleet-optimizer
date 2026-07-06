package com.routes.service;

import com.routes.model.entity.Leg;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for calculating distances and durations using external geocoding APIs.
 * Currently a placeholder - will be implemented with OSRM or similar API.
 */
@Service
@Slf4j
@RequiredArgsConstructor        // necesario para poder testear
public class GeocodingService {

    /**
     * Calculates distance and duration for each leg.
     * 
     * @param legs List of legs to calculate for
     * @return Updated legs with distanceKm and durationMinutes set
     */
    public List<Leg> calculateLegDistances(List<Leg> legs) {
        log.info("Calculating distances for {} legs", legs.size());
        
        // TODO: Implement with actual geocoding API (OSRM, Google Maps, etc.)
        // For now, sets default values
        for (Leg leg : legs) {
            // Placeholder: set default values
            if (leg.getDistanceKm() == null) {
                leg.setDistanceKm(0.0);
            }
            if (leg.getDurationMinutes() == null) {
                leg.setDurationMinutes(0);
            }
            log.debug("Leg {}: distance={}km, duration={}min", 
                leg.getSequence(), leg.getDistanceKm(), leg.getDurationMinutes());
        }
        
        return legs;
    }
}