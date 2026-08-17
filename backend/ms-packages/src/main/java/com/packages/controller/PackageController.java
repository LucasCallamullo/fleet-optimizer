package com.packages.controller;

import com.packages.dto.external.PackageDTO;
import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.request.PackageStatusUpdateRequest;
import com.packages.dto.response.PackageDetailDTO;
import com.packages.dto.response.PackageResponseDTO;
import com.packages.exception.AppException;
import com.packages.service.PackageExternalService;
import com.packages.service.PackageService;
import com.packages.utils.AuthHelper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@Slf4j
@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
public class PackageController {

    private final PackageService packageService;
    
    private final PackageExternalService packageExternalService;

    // necesary for some utils on request propagate from gateway
    private final AuthHelper authHelper;    

    // ================================================================
    // CREATE
    // ================================================================

    /**
     * Creates a new package.
     * 
     * Endpoint: POST /api/v1/packages
     * 
     * @param request Package creation data
     * @param ownerId User ID from Gateway header
     * @return Created package with full details
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    public PackageDetailDTO createPackage(
            @Valid @RequestBody PackageRequestDTO request,
            @RequestHeader(value = "X-User-Id", required = false) String ownerId) {
        
        log.info("POST /api/v1/packages - Creating package with tracking: {}", request.trackingNumber());
        
        return packageService.createPackage(request, ownerId);
    }

    // ================================================================
    // READ
    // ================================================================

    // ================================================================
    // 1. GET ALL PACKAGES (Filtered by User)
    // ================================================================

    /**
     * GET /api/v1/packages
     * 
     * Returns packages filtered by the current user.
     * Admin users see all packages. Regular users see only their own.
     * 
     * @param request - HttpServletRequest containing Gateway headers
     * @return List<PackageResponseDTO> - Filtered list of packages
     * @throws AppException - 401 if user is not authenticated
     */
    @GetMapping
    public List<PackageResponseDTO> getPackages(HttpServletRequest request) {
        // Extract user ID and admin status from Gateway headers
        String userId = authHelper.getCurrentUserId(request);
        boolean isAdmin = authHelper.isAdmin(request);
        
        log.info("GET /api/v1/packages - User: {}, Admin: {}", userId, isAdmin);
        
        return packageService.getPackagesForUser(userId, isAdmin);
    }

    // ================================================================
    // 2. GET DETAILED PACKAGES (With Store Information)
    // ================================================================

    /**
     * GET /api/v1/packages/detailed
     * 
     * Returns packages with full store details filtered by user.
     * Admin users see all packages. Regular users see only their own.
     * 
     * @param request - HttpServletRequest containing Gateway headers
     * @return List<PackageDetailDTO> - Filtered list of packages with store details
     * @throws AppException - 401 if user is not authenticated
     */
    @GetMapping("/detailed")
    public List<PackageDetailDTO> getPackagesDetailed(HttpServletRequest request) {
        // Extract user ID and admin status from Gateway headers
        String userId = authHelper.getCurrentUserId();
        boolean isAdmin = authHelper.isAdmin();
        
        log.info("GET /api/v1/packages/detailed - User: {}, Admin: {}", userId, isAdmin);
        
        return packageService.getDetailedPackagesForUser(userId, isAdmin);
    }

    /**
     * Retrieves a package by ID (basic view).
     * 
     * Endpoint: GET /api/v1/packages/{id}
     * 
     * @param id Package ID
     * @return Basic package response
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PackageResponseDTO getPackage(@PathVariable Long id) {
        log.info("GET /api/v1/packages/{} - Fetching package (basic)", id);
        return packageService.getPackage(id);
    }

    /**
     * Retrieves a package by ID with full store details.
     * 
     * Endpoint: GET /api/v1/packages/{id}/detailed
     * 
     * @param id Package ID
     * @return Detailed package response with store information
     */
    @GetMapping("/{id}/detailed")
    @PreAuthorize("isAuthenticated()")
    public PackageDetailDTO getPackageDetailed(@PathVariable Long id) {
        log.info("GET /api/v1/packages/{}/detailed - Fetching package with store details", id);
        return packageService.getPackageDetail(id);
    }

    // ================================================================
    // UPDATE
    // ================================================================

    /**
     * Updates an existing package.
     * 
     * Endpoint: PUT /api/v1/packages/{id}
     * 
     * @param id Package ID
     * @param request Updated package data
     * @return Updated package with full details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PackageDetailDTO updatePackage(
            @PathVariable Long id,
            @Valid @RequestBody PackageRequestDTO request) {
        
        log.info("PUT /api/v1/packages/{} - Updating package", id);
        return packageService.updatePackage(id, request);
    }

    // ================================================================
    // DELETE
    // ================================================================

    /**
     * Deletes a package by ID.
     * 
     * Endpoint: DELETE /api/v1/packages/{id}
     * 
     * @param id Package ID
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePackage(@PathVariable Long id) {
        log.info("DELETE /api/v1/packages/{} - Deleting package", id);
        packageService.deletePackage(id);
    }

    // ================================================================
    // INTER-SERVICE COMMUNICATION (para ms-routes)
    // ================================================================

    /**
     * Fetches package details by their IDs.
     * Used exclusively by ms-routes for shipment creation.
     * 
     * Endpoint: GET /api/v1/packages/internal?ids=1,2,3
     * 
     * This endpoint is INTERNAL - only accessible from other services.
     * No authentication required.
     * 
     * @param ids List of package IDs (comma-separated)
     * @return List of PackageDTO with package details
     */
    @GetMapping(value = "/internal", params = "ids")
    public List<PackageDTO> getPackagesByIds(@RequestParam("ids") List<Long> ids) {
        log.info("GET /api/v1/packages/internal?ids={} - Fetching packages for ms-routes", ids);
        return packageExternalService.getPackagesDto(ids);
    }

    /**
     * Updates the status of one or more packages.
     * 
     * Endpoint: PATCH /api/v1/packages/status
     * 
     * Request body:
     * {
     *   "packageIds": [1, 2, 3],
     *   "status": "IN_TRANSIT"
     * }
     * 
     * @param request The status update request
     * @throws AppException if any package not found
     * @throws AppException if status is invalid
     * @throws AppException if packages are not in valid state
     */
    // TODO: Consider migrating endpoint back to @PatchMapping once Feign client in ms-routes 
    // is upgraded with feign-hc5 (Apache HttpClient 5) to support HTTP PATCH.
    @PutMapping("/status")
    @PreAuthorize("isAuthenticated()")
    public void updatePackageStatus(@Valid @RequestBody PackageStatusUpdateRequest request) {
        log.info("PATCH /api/v1/packages/status - Updating {} packages to '{}'", 
            request.packageIds().size(), request.status());
        packageService.updatePackageStatus(request.packageIds(), request.status());
    }
}