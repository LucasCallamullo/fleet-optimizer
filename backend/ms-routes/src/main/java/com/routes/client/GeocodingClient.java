package com.routes.client;

import com.routes.config.FeignConfig;
import com.routes.dto.client.geocoding.BatchDistanceRequest;
import com.routes.dto.client.geocoding.BatchDistanceResponse;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "ms-geocoding",
    url = "${app.clients.geocoding.url:http://localhost:8084}",
    configuration = FeignConfig.class
)
public interface GeocodingClient {
    
    /**
     * Calculates distances and durations for multiple location pairs in a single request.
     * 
     * Endpoint: POST /api/v1/distance/batch
     * 
     * Request:
     * {
     *   "locations": [
     *     {
     *       "legId": 1,
     *       "origin": { "latitude": -34.6037, "longitude": -58.3816 },
     *       "destination": { "latitude": -31.4201, "longitude": -64.1888 }
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
     *     }
     *   ]
     * }
     */
    @PostMapping("/api/v1/distance/batch")
    BatchDistanceResponse calculateDistances(@RequestBody BatchDistanceRequest request);
}