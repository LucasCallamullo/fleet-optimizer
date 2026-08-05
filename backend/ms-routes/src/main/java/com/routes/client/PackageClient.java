package com.routes.client;

import com.routes.config.FeignConfig;
import com.routes.dto.client.packages.PackageDTO;
import com.routes.dto.client.packages.PackageStatusUpdateRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for communicating with the Package Microservice (ms-packages).
 * 
 * This client provides a declarative way to call the Package MS REST API.
 * Spring Cloud Feign generates the implementation at runtime.
 * 
 * The response includes package details such as:
 *   - id: Unique package identifier
 *   - trackingNumber: Shipment tracking code
 *   - totalWeightKg: Total weight of the package in kilograms
 *   - totalVolumeCbm: Total volume of the package in cubic meters
 */
@FeignClient(
    name = "ms-packages",                                           // ← Service name (for service discovery)
    url = "${app.clients.packages.url:http://localhost:8083}",       // ← Configurable URL with fallback
    configuration = FeignConfig.class                // intercepts errors from other MS
)
public interface PackageClient {
    
    /**
     * Fetches package details by their IDs from the Package MS.
     * 
     * Endpoint: GET /api/v1/packages?ids=100,101,102
     * 
     * Spring Cloud Feign will automatically:
     * 1. Serialize the list of IDs to a comma-separated string (ids=100,101,102)
     * 2. Make the HTTP GET request to the configured URL
     * 3. Deserialize the JSON response to List<PackageDTO>
     * 4. Handle retries and error responses (if configured)
     * 
     * The Package MS is responsible for:
     *   - Storing package metadata (weight, volume, tracking number)
     *   - Managing package status and history
     *   - Providing package details for route planning
     * 
     * This client is used by ms-routes to:
     *   - Validate that packages exist before assigning them to legs
     *   - Retrieve weight and volume for capacity validation
     *   - Ensure packages are not duplicated across legs
     * 
     * @param ids List of package IDs to fetch
     * @return List of PackageDTO objects with package details
     * @throws org.springframework.web.client.HttpClientErrorException if any package is not found (404)
     * @throws org.springframework.web.client.HttpServerErrorException if Package MS returns an error (5xx)
     */
    @GetMapping("/api/v1/packages")
    List<PackageDTO> getPackagesByIds(@RequestParam("ids") List<Long> ids);

    /**
     * Updates the status of multiple packages.
     * 
     * Endpoint: PATCH /api/v1/packages/status
     * 
     * This is used by ms-routes when a shipment is created to update
     * package statuses from READY_FOR_PICKUP to IN_TRANSIT.
     * 
     * Request body:
     * {
     *   "packageIds": [1, 2, 3],
     *   "status": "IN_TRANSIT"
     * }
     * 
     * @param request The status update request containing package IDs and new status
     */
    @PatchMapping("/api/v1/packages/status")
    void updatePackageStatus(@RequestBody PackageStatusUpdateRequest request);

    /**
     * Hardcoded packages for testing purposes.
     * TODO: Remove this method when ms-packages is available.
     *
    private List<PackageDTO> getHardcodedPackages() {
        return List.of(
            new PackageDTO(1L, "PKG-001", 10.0, 10.0),
            new PackageDTO(2L, "PKG-002", 20.0, 20.0),
            new PackageDTO(3L, "PKG-003", 30.0, 30.0),
            new PackageDTO(4L, "PKG-004", 15.0, 15.0),
            new PackageDTO(5L, "PKG-005", 25.0, 25.0)
        );
    } */
}