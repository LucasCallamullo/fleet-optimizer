package com.geocoding.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
import com.geocoding.dto.request.BatchDistanceRequestDTO;
import com.geocoding.dto.request.DistanceRequestDTO;
import com.geocoding.dto.request.LocationPairDTO;
import com.geocoding.dto.response.BatchDistanceResponseDTO;
import com.geocoding.dto.response.DistanceResponseDTO;
import com.geocoding.dto.response.DistanceResultDTO;
import com.geocoding.exception.AppException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrsService {

    private final WebClient orsWebClient;
    // private final ObjectMapper objectMapper;

    @Value("${app.ors.api-key}")
    private String apiKey;

    /**
     * Calculates distance and duration between two coordinates using ORS API.
     * 
     * Step-by-step flow:
     * 1. Build the request body with coordinates in the format ORS expects
     *    - ORS uses [longitude, latitude] order (GeoJSON standard)
     *    - Coordinates array: [[lon1, lat1], [lon2, lat2]]
     * 
     * 2. Make a POST request to ORS Directions API
     *    - Endpoint: /v2/directions/driving-car/geojson
     *    - Returns route as GeoJSON (includes geometry, distance, duration)
     *    - API key is sent in Authorization header
     * 
     * 3. Handle errors: if ORS returns an error status, throw AppException
     * 
     * 4. Parse the response using parseOrsResponse():
     *    - Extracts "features" array from GeoJSON response
     *    - Gets "summary" object containing total distance (meters) and duration (seconds)
     *    - Converts distance to kilometers and duration to minutes
     *    - Also extracts the full geometry (path coordinates) as a JSON string
     * 
     * 5. Return DistanceResponseDTO with distanceKm, durationMinutes, and geometry
     * 
     * The "geometry" field is optional and contains the complete route path
     * as a LineString with all coordinates. This can be used to draw the route
     * on a map in the frontend.
     * 
     * @param request Origin and destination coordinates
     * @return DistanceResponseDTO with distance in km, duration in minutes, and route geometry
     */
    public Mono<DistanceResponseDTO> calculateDistance(DistanceRequestDTO request) {
        log.info("Calculating distance between ({}, {}) and ({}, {})",
            request.originLat(), request.originLon(),
            request.destLat(), request.destLon());

        // Step 1: Build request body for ORS API
        // ORS expects coordinates in [longitude, latitude] format
        // GeoJSON standard uses lon,lat order, not lat,lon
        Map<String, Object> body = Map.of(
            "coordinates", new double[][]{
                {request.originLon(), request.originLat()},  // [lon, lat] order
                {request.destLon(), request.destLat()}        // [lon, lat] order
            }
        );

        // Step 2: Make POST request to ORS Directions API
        // The response is a GeoJSON object containing the route
        return orsWebClient.post()
            .uri("/v2/directions/driving-car/geojson")
            .header("Authorization", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            
            // Step 3: Handle errors
            .onStatus(status -> status.isError(), response -> {
                log.error("ORS API error: {}", response.statusCode());
                return response.bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new AppException("Error calculating distance: " + error, 500)));
            })
            
            // Step 4: Parse response
            .bodyToMono(JsonNode.class)
            .map(response -> parseOrsResponse(response));
    }

    /**
     * Parses the ORS API GeoJSON response and extracts distance, duration, and geometry.
     * 
     * Step-by-step:
     * 1. Navigate to "features[0].properties.summary" to get route summary
     * 2. Extract "distance" (in meters) and "duration" (in seconds)
     * 3. Convert to km and minutes for human-readable format
     * 4. Extract "features[0].geometry" as a JSON string for map visualization
     * 5. Return as DistanceResponseDTO
     * 
     * Example ORS response structure (simplified):
     * {
     *   "features": [
     *     {
     *       "geometry": {
     *         "type": "LineString",
     *         "coordinates": [[lon1, lat1], [lon2, lat2], ...]
     *       },
     *       "properties": {
     *         "summary": {
     *           "distance": 700500.0,   // in meters
     *           "duration": 28800.0     // in seconds
     *         }
     *       }
     *     }
     *   ]
     * }
     * 
     * @param response The raw JSON response from ORS
     * @return DistanceResponseDTO with parsed values
     */
    private DistanceResponseDTO parseOrsResponse(JsonNode response) {
        try {
            // Step 1: Navigate to the summary object
            // Path: features[0].properties.summary
            JsonNode summary = response.path("features")
                .path(0)
                .path("properties")
                .path("summary");

            // Step 2: Extract distance and duration
            double distanceMeters = summary.path("distance").asDouble();
            double durationSeconds = summary.path("duration").asDouble();

            // Step 3: Convert to human-readable units
            // 1 km = 1000 meters, 1 minute = 60 seconds
            double distanceKm = distanceMeters / 1000.0;
            int durationMinutes = (int) (durationSeconds / 60.0);

            log.debug("Parsed ORS response - Distance: {} km, Duration: {} minutes", 
                distanceKm, durationMinutes);

            // Step 4: Extract geometry as JSON string (optional, for map visualization)
            // This contains the full route path as a LineString with all coordinates
            JsonNode geometry = response.path("features")
                .path(0)
                .path("geometry");
            
            // Step 5: Return DTO with all extracted values
            return new DistanceResponseDTO(
                distanceKm,                           // Human-readable distance
                durationMinutes,                      // Human-readable duration
                geometry.toString()                   // Full route path as JSON (for maps)
            );
            
        } catch (Exception e) {
            log.error("Failed to parse ORS response: {}", e.getMessage());
            throw new AppException("Failed to parse distance response", 500);
        }
    }


    /**
     * Calculates distances and durations for multiple location pairs in batch.
     * 
     * Uses ORS Matrix API which calculates all combinations in a single request.
     * 
     * Step-by-step:
     * 1. Extract all unique origins and destinations from the request
     * 2. Build the ORS Matrix request with all locations
     * 3. Make a single POST request to ORS Matrix API
     * 4. Parse the response and extract distance/duration for each pair
     * 5. Map results back to the original leg IDs
     * 
     * @param request Batch request with multiple location pairs
     * @return BatchDistanceResponseDTO with results for each pair
     */
    public Mono<BatchDistanceResponseDTO> calculateBatchDistances(BatchDistanceRequestDTO request) {
        log.info("Calculating batch distances for {} pairs", request.pairs().size());

        // Step 1: Extract all locations (origins and destinations)
        // For Matrix API, we need all unique locations in one array
        List<double[]> allLocations = new ArrayList<>();
        List<Integer> originIndices = new ArrayList<>();
        List<Integer> destIndices = new ArrayList<>();

        // First, add all origins
        for (LocationPairDTO pair : request.pairs()) {
            double[] origin = {pair.originLon(), pair.originLat()};
            allLocations.add(origin);
            originIndices.add(allLocations.size() - 1);
        }

        // Then, add all destinations
        for (LocationPairDTO pair : request.pairs()) {
            double[] dest = {pair.destLon(), pair.destLat()};
            allLocations.add(dest);
            destIndices.add(allLocations.size() - 1);
        }

        // Step 2: Build request body for ORS Matrix API
        // Matrix API calculates distances between all sources and all destinations
        Map<String, Object> body = Map.of(
            "locations", allLocations,
            "metrics", new String[]{"distance", "duration"},
            "sources", originIndices,
            "destinations", destIndices
        );

        // Step 3: Make POST request to ORS Matrix API
        return orsWebClient.post()
            .uri("/v2/matrix/driving-car")
            .header("Authorization", apiKey)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(body)
            .retrieve()
            .onStatus(status -> status.isError(), response -> {
                log.error("ORS Matrix API error: {}", response.statusCode());
                return response.bodyToMono(String.class)
                    .flatMap(error -> Mono.error(new AppException("Error calculating batch distances: " + error, 500)));
            })
            .bodyToMono(JsonNode.class)
            .map(response -> parseMatrixResponse(response, request));
    }

    /**
     * Parses the ORS Matrix response and maps results back to leg IDs.
     * 
     * Step-by-step:
     * 1. Extract distances and durations from the Matrix response
     * 2. The matrix is [sources x destinations] where sources = origins and destinations = destinations
     * 3. For each pair, we get the corresponding matrix cell
     * 4. Map each result to the original legId
     * 
     * @param response The raw JSON response from ORS
     * @param request The original request with leg IDs
     * @return BatchDistanceResponseDTO with mapped results
     */
    private BatchDistanceResponseDTO parseMatrixResponse(JsonNode response, BatchDistanceRequestDTO request) {
        try {
            // Step 1: Extract distances and durations from response
            // Matrix API returns:
            // - distances: 2D array [sources][destinations] in meters
            // - durations: 2D array [sources][destinations] in seconds
            JsonNode distancesNode = response.path("distances");
            JsonNode durationsNode = response.path("durations");

            List<DistanceResultDTO> results = new ArrayList<>();

            // Step 2: Iterate over each pair and get the corresponding matrix cell
            // Each origin i maps to destination i (same index)
            for (int i = 0; i < request.pairs().size(); i++) {
                LocationPairDTO pair = request.pairs().get(i);
                
                // Extract distance in meters and convert to km
                double distanceMeters = distancesNode.path(i).path(i).asDouble();
                double distanceKm = distanceMeters / 1000.0;
                
                // Extract duration in seconds and convert to minutes
                double durationSeconds = durationsNode.path(i).path(i).asDouble();
                int durationMinutes = (int) (durationSeconds / 60.0);

                log.debug("Pair {}: Distance: {} km, Duration: {} minutes", 
                    pair.legId(), distanceKm, durationMinutes);

                // Step 3: Build result with original legId
                results.add(new DistanceResultDTO(
                    pair.legId(),
                    distanceKm,
                    durationMinutes
                ));
            }

            return new BatchDistanceResponseDTO(results);

        } catch (Exception e) {
            log.error("Failed to parse ORS Matrix response: {}", e.getMessage(), e);
            throw new AppException("Failed to parse batch distance response", 500);
        }
    }
}
