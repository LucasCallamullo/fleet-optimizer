package com.packages.service;

import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.response.PackageDetailDTO;
import com.packages.dto.response.PackageResponseDTO;
import com.packages.exception.AppException;

import java.util.List;

/**
 * Service interface for managing Package entities.
 * 
 * This service provides comprehensive CRUD operations for packages
 * in the logistics system. Each package belongs to a store and has
 * a tracking number, weight, volume, and status.
 * 
 * All methods throw AppException with appropriate HTTP status codes:
 * - 400: Bad Request (validation errors)
 * - 404: Not Found (package or store not found)
 * - 409: Conflict (duplicate tracking number)
 * - 500: Internal Server Error (unexpected errors)
 */
public interface PackageService {


    // ================================================================
    // STATUS MANAGEMENT (specific business operation)
    // ================================================================

    /**
     * Updates the status of multiple packages.
     * 
     * This is a specific business operation used by ms-routes when
     * a shipment is created. It transitions packages from READY_FOR_PICKUP
     * to IN_TRANSIT.
     * 
     * Business Rules:
     * - All packages must exist (throws 404 if any not found)
     * - Status must be a valid PackageStatus value
     * - Only packages with status READY_FOR_PICKUP can be changed to IN_TRANSIT
     * 
     * @param packageIds List of package IDs to update
     * @param status The new status (as String, e.g., "IN_TRANSIT")
     * @throws AppException if any package not found (404)
     * @throws AppException if status is invalid (400)
     * @throws AppException if packages are not in valid state for transition (400)
     */
    void updatePackageStatus(List<Long> packageIds, String status);


    // ================================================================
    // CREATE
    // ================================================================

    /**
     * Creates a new package with the provided data and owner.
     * 
     * <p><strong>Business Rules:</strong>
     * <ul>
     *   <li>Tracking number must be unique (throws 409 if exists)</li>
     *   <li>Store must exist (throws 404 if not found)</li>
     *   <li>Status is automatically set to CREATED</li>
     *   <li>Owner ID is set from authenticated user (Gateway header)</li>
     * </ul>
     * 
     * @param request The package creation data (trackingNumber, weight, volume, storeId)
     * @param ownerId The user ID from Keycloak (X-User-Id header from Gateway)
     * @return PackageDetailDTO with all package details including the store
     * @throws AppException with status 409 if tracking number already exists
     * @throws AppException with status 404 if store not found
     */
    PackageDetailDTO createPackage(PackageRequestDTO request, String ownerId);

    // ================================================================
    // READ
    // ================================================================

    /**
     * Retrieves all packages with basic information (no store details).
     * 
     * This method does NOT load the store relationship (LAZY loading).
     * Use {@link #getAllPackagesWithStore()} for full store details.
     * 
     * @return List of PackageResponseDTO containing id, trackingNumber,
     *         weight, volume, status, storeId, and ownerId
     */
    List<PackageResponseDTO> getAllPackages();

    /**
     * Retrieves all packages with full store details.
     * 
     * This method uses JOIN FETCH to load the store relationship
     * eagerly, avoiding LazyInitializationException. The response
     * includes the complete store information including location.
     * 
     * @return List of PackageDetailDTO with full store details
     */
    List<PackageDetailDTO> getAllPackagesWithStore();

    /**
     * Retrieves a package by ID with basic information (no store details).
     * 
     * @param id The package ID (must be positive)
     * @return PackageResponseDTO with basic package information
     * @throws AppException with status 404 if package not found
     */
    PackageResponseDTO getPackage(Long id);

    /**
     * Retrieves a package by ID with full store details.
     * 
     * This method uses JOIN FETCH to load the store relationship
     * eagerly, avoiding LazyInitializationException. The response
     * includes the complete store information including location.
     * 
     * @param id The package ID (must be positive)
     * @return PackageDetailDTO with full package and store details
     * @throws AppException with status 404 if package not found
     */
    PackageDetailDTO getPackageDetail(Long id);

    // ================================================================
    // UPDATE
    // ================================================================

    /**
     * Updates an existing package with new data.
     * 
     * Business Rules:
     * <ul>
     *   <li>Package must exist (throws 404 if not found)</li>
     *   <li>Tracking number must be unique (throws 409 if conflict)</li>
     *   <li>Store must exist if changing store (throws 404 if not found)</li>
     *   <li>All fields in the request will overwrite existing values</li>
     * </ul>
     * 
     * @param id The package ID to update
     * @param request The updated package data
     * @return PackageDetailDTO with updated package and store details
     * @throws AppException with status 404 if package not found
     * @throws AppException with status 409 if tracking number conflict
     * @throws AppException with status 404 if new store not found
     */
    PackageDetailDTO updatePackage(Long id, PackageRequestDTO request);

    // ================================================================
    // DELETE
    // ================================================================

    /**
     * Deletes a package by ID.
     * 
     * <p><strong>Business Rules:</strong>
     * <ul>
     *   <li>Package must exist (throws 404 if not found)</li>
     *   <li>Deletion is permanent and cannot be undone</li>
     *   <li>No cascading effects on other entities</li>
     * </ul>
     * 
     * @param id The package ID to delete
     * @throws AppException with status 404 if package not found
     */
    void deletePackage(Long id);

    // ================================================================
    // USER-FILTERED PACKAGES (NEW METHODS)
    // ================================================================

    /**
     * Retrieves packages filtered by user ID.
     * 
     * If the user is an admin, returns all packages.
     * If the user is a regular user, returns only packages owned by them.
     * 
     * This method returns basic information without store details.
     * 
     * @param userId - The ID of the current user (from X-User-Id header)
     * @param isAdmin - True if the user has admin role (from X-User-Roles header)
     * @return List<PackageResponseDTO> - Filtered list of packages
     */
    List<PackageResponseDTO> getPackagesForUser(String userId, boolean isAdmin);

    /**
     * Retrieves packages with store details filtered by user ID.
     * 
     * If the user is an admin, returns all packages with store details.
     * If the user is a regular user, returns only packages owned by them
     * with store details.
     * 
     * This method uses JOIN FETCH to load the store relationship eagerly,
     * avoiding LazyInitializationException.
     * 
     * @param userId - The ID of the current user (from X-User-Id header)
     * @param isAdmin - True if the user has admin role (from X-User-Roles header)
     * @return List<PackageDetailDTO> - Filtered list of packages with store details
     */
    List<PackageDetailDTO> getDetailedPackagesForUser(String userId, boolean isAdmin);
}