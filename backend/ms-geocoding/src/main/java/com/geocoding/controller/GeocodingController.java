package com.geocoding.controller;

import com.geocoding.dto.request.DistanceRequestDTO;
import com.geocoding.dto.request.BatchDistanceRequestDTO;
import com.geocoding.dto.response.DistanceResponseDTO;
import com.geocoding.dto.response.BatchDistanceResponseDTO;
import com.geocoding.service.OrsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/v1/distance")
@RequiredArgsConstructor
public class GeocodingController {

    private final OrsService orsService;

    /**
     * Calculates distance and duration between two coordinates.
     * 
     * Endpoint: POST /api/v1/distance
     * 
     * Request body:
     * {
     *   "originLat": -34.6037,
     *   "originLon": -58.3816,
     *   "destLat": -31.4201,
     *   "destLon": -64.1888
     * }
     * 
     * Response:
     * {
     *   "distanceKm": 700.5,
     *   "durationMinutes": 480,
     *   "geometry": "{...}"
     * }
     */
    @PostMapping
    public Mono<DistanceResponseDTO> calculateDistance(@Valid @RequestBody DistanceRequestDTO request) {
        log.info("POST /api/v1/distance - Calculating distance");
        return orsService.calculateDistance(request);
    }

    /**
     * Calculates distances and durations for multiple location pairs (batch).
     * 
     * Uses ORS Matrix API to calculate all distances in a single request.
     * 
     * Endpoint: POST /api/v1/distance/batch
     * 
     * Request body:
     * {
     *   "pairs": [
     *     {
     *       "legId": 1,
     *       "originLat": -34.6037,
     *       "originLon": -58.3816,
     *       "destLat": -31.4201,
     *       "destLon": -64.1888
     *     },
     *     {
     *       "legId": 2,
     *       "originLat": -31.4201,
     *       "originLon": -64.1888,
     *       "destLat": -32.8908,
     *       "destLon": -68.8272
     *     }
     *   ]
     * }
     * 
     * Response:
     * {
     *   "results": [
     *     {
     *       "legId": 1,
     *       "distanceKm": 700.5,
     *       "durationMinutes": 480
     *     },
     *     {
     *       "legId": 2,
     *       "distanceKm": 650.3,
     *       "durationMinutes": 420
     *     }
     *   ]
     * }
     */
    @PostMapping("/batch")
    public Mono<BatchDistanceResponseDTO> calculateBatchDistances(@Valid @RequestBody BatchDistanceRequestDTO request) {
        log.info("POST /api/v1/distance/batch - Calculating {} distances", request.pairs().size());
        return orsService.calculateBatchDistances(request);
    }
}
