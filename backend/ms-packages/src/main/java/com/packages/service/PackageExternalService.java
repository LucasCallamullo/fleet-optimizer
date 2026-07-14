package com.packages.service;

import com.packages.dto.external.PackageDTO;
import com.packages.exception.AppException;
import com.packages.mapper.PackageExternalMapper;
import com.packages.model.entity.Package;
import com.packages.repository.PackageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * External service for inter-microservice communication.
 * 
 * This service provides package data to other microservices (ms-routes, ms-fleets)
 * in a format optimized for external consumption.
 * 
 * <p><strong>Key Features:</strong>
 * <ul>
 *   <li>Uses JOIN FETCH to load stores eagerly (no LazyInitializationException)</li>
 *   <li>Maps entities to lightweight PackageDTOs</li>
 *   <li>Includes store location (origin) for route calculation</li>
 *   <li>Handles both single and batch requests</li>
 * </ul>
 * 
 * <p><strong>Usage Example (ms-routes Feign Client):</strong>
 * <pre>
 * GET /api/v1/packages?ids=1,2,3
 * Response: [
 *   {
 *     "id": 1,
 *     "totalWeightKg": 10.0,
 *     "totalVolumeCbm": 0.30,
 *     "origin": {
 *       "street": "Av. Libertador",
 *       "city": "Buenos Aires",
 *       "latitude": -34.6037,
 *       "longitude": -58.3816
 *     }
 *   }
 * ]
 * </pre>
 * 
 * <p><strong>Why no interface?</strong>
 * This service has a single, specific responsibility: fetching package data
 * for external microservices. There's no need for an interface since there's
 * only one implementation and no alternative data sources.
 * 
 * @see PackageDTO
 * @see PackageExternalMapper
 * @see PackageRepository#findAllByIdWithStore(List)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PackageExternalService {

    private final PackageRepository packageRepository;
    private final PackageExternalMapper packageExternalMapper;

    /**
     * Fetches multiple packages by their IDs and maps them to PackageDTOs.
     * 
     * <p><strong>Behavior:</strong>
     * <ul>
     *   <li>Uses JOIN FETCH to load store relationship eagerly</li>
     *   <li>Returns empty list if no packages found (not an error)</li>
     *   <li>Maps store.location to origin field in PackageDTO</li>
     * </ul>
     * 
     * @param ids List of package IDs to fetch (must not be null)
     * @return List of PackageDTO containing id, weight, volume, and origin location
     * @throws AppException if any validation fails (currently none, but reserved for future)
     */
    public List<PackageDTO> getPackagesDto(List<Long> ids) {
        log.debug("Fetching package DTOs for {} IDs", ids != null ? ids.size() : 0);
        
        // Step 1: Validate input
        if (ids == null || ids.isEmpty()) {
            log.warn("Received empty or null IDs list, returning empty list");
            return List.of();
        }

        // Step 2: Fetch packages with store eagerly loaded (JOIN FETCH)
        // This ensures we don't get LazyInitializationException when accessing store.location
        List<Package> packages = packageRepository.findAllByIdWithStore(ids);

        // Step 3: Early return if no packages found
        if (packages.isEmpty()) {
            log.debug("No packages found for IDs: {}", ids);
            return List.of();
        }
        
        log.debug("Found {} packages matching the requested IDs", packages.size());

        // Step 4: Map to DTOs
        List<PackageDTO> dtos = packageExternalMapper.toPackageDtoList(packages);
        
        log.debug("Mapped {} packages to PackageDTOs", dtos.size());
        return dtos;
    }

    /**
     * Fetches a single package by its ID and maps it to PackageDTO.
     * 
     * <p><strong>Behavior:</strong>
     * <ul>
     *   <li>Uses JOIN FETCH to load store relationship eagerly</li>
     *   <li>Throws AppException with 404 if package not found</li>
     *   <li>Maps store.location to origin field in PackageDTO</li>
     * </ul>
     * 
     * @param id The package ID to fetch (must be positive)
     * @return PackageDTO containing id, weight, volume, and origin location
     * @throws AppException with status 404 if package not found
     */
    public PackageDTO getPackageDto(Long id) {
        log.debug("Fetching package DTO for id: {}", id);

        // Step 1: Validate input
        if (id == null || id <= 0) {
            throw new AppException("Invalid package ID: " + id, 400);
        }

        // Step 2: Fetch package with store eagerly loaded (JOIN FETCH)
        // This ensures we don't get LazyInitializationException when accessing store.location
        Package pkg = packageRepository.findByIdWithStore(id)
            .orElseThrow(() -> {
                log.warn("Package not found with id: {}", id);
                return new AppException("Package not found: " + id, 404);
            });

        // Step 3: Map to DTO
        PackageDTO dto = packageExternalMapper.toPackageDto(pkg);
        
        log.debug("Mapped package {} to PackageDTO", id);
        return dto;
    }
}