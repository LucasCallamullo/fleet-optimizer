package com.routes.client;

import com.routes.config.FeignConfig;
import com.routes.dto.external.FleetVehicleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with the Fleet Microservice (ms-fleets).
 * 
 * This client provides a declarative way to call the Fleet MS REST API.
 * Spring Cloud Feign generates the implementation at runtime.
 * 
 * Example:
 *   List<FleetVehicleDTO> vehicles = fleetClient.getVehiclesByIds(List.of(1L, 2L, 3L));
 */
@FeignClient(
    name = "ms-fleets",                                           // ← Service name (for service discovery)
    url = "${app.clients.fleets.url:http://localhost:8081}",       // ← Configurable URL with fallback
    configuration = FeignConfig.class                // intercepts errors from other MS
)
public interface FleetClient {
    
    /**
     * Fetches vehicle details by their IDs from the Fleet MS.
     * 
     * Endpoint: GET /api/v1/vehicles?ids=1,2,3
     * 
     * Spring Cloud Feign will automatically:
     * 1. Serialize the list of IDs to a comma-separated string (ids=1,2,3)
     * 2. Make the HTTP GET request to the configured URL
     * 3. Deserialize the JSON response to List<FleetVehicleDTO>
     * 4. Handle retries and error responses (if configured)
     * 
     * @param ids List of vehicle IDs to fetch
     * @return List of FleetVehicleDTO objects with vehicle details
     * @throws org.springframework.web.client.HttpClientErrorException if any vehicle is not found (404)
     * @throws org.springframework.web.client.HttpServerErrorException if Fleet MS returns an error (5xx)
     */
    @GetMapping("/api/v1/vehicles")
    List<FleetVehicleDTO> getVehiclesByIds(@RequestParam("ids") List<Long> ids);
}