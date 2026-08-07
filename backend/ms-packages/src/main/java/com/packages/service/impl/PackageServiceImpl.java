package com.packages.service.impl;

import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.response.PackageDetailDTO;
import com.packages.dto.response.PackageResponseDTO;
import com.packages.exception.AppException;
import com.packages.mapper.PackageMapper;
import com.packages.model.entity.Package;
import com.packages.model.entity.Store;
import com.packages.model.enums.PackageStatus;
import com.packages.repository.PackageRepository;
import com.packages.repository.StoreRepository;
import com.packages.service.PackageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PackageServiceImpl implements PackageService {

    private final PackageRepository packageRepository;
    private final StoreRepository storeRepository;
    private final PackageMapper packageMapper;

    // ================================================================
    // CREATE
    // ================================================================

    @Override
    @Transactional
    public PackageDetailDTO createPackage(PackageRequestDTO request, String ownerId) {

        // Step 1: Validate ownerId
        if (ownerId == null || ownerId.isEmpty()) {
            throw new AppException("User ID is required to create a package", 400);
        }

        log.info("Creating package with tracking number: {}", request.trackingNumber());

        // Step 1: Validate tracking number is unique
        if (packageRepository.findByTrackingNumber(request.trackingNumber()).isPresent()) {
            throw new AppException("Tracking number already exists: " + request.trackingNumber(), 409);
        }

        // Step 2: Validate store exists
        Store store = storeRepository.findById(request.storeId())
            .orElseThrow(() -> new AppException("Store not found: " + request.storeId(), 404));

        // Step 3: Create entity
        Package pkg = packageMapper.toEntity(request);
        pkg.setStore(store);
        pkg.setOwnerId(ownerId);
        pkg.setStatus(PackageStatus.CREATED);

        // Step 4: Save
        Package saved = packageRepository.save(pkg);
        log.info("Package created with id: {}", saved.getId());
        saved.setStore(store);

        // Step 5: Return detail with store loaded
        return packageMapper.toDetailDto(pkg);
    }

    // ================================================================
    // READ
    // ================================================================

    @Override
    public List<PackageResponseDTO> getAllPackages() {
        log.debug("Fetching all packages (basic view)");
        List<Package> packages = packageRepository.findAll();
        return packageMapper.toResponseDtoList(packages);
    }

    @Override
    public List<PackageDetailDTO> getAllPackagesWithStore() {
        log.debug("Fetching all packages with store details");
        List<Package> packages = packageRepository.findAllWithStore();
        return packageMapper.toDetailDtoList(packages);
    }

    @Override
    public PackageResponseDTO getPackage(Long id) {
        log.debug("Fetching package by id: {}", id);
        Package pkg = packageRepository.findById(id)
            .orElseThrow(() -> new AppException("Package not found: " + id, 404));
        return packageMapper.toResponseDto(pkg);
    }

    @Override
    public PackageDetailDTO getPackageDetail(Long id) {
        log.debug("Fetching package detail with store for id: {}", id);
        Package pkg = packageRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AppException("Package not found: " + id, 404));
        return packageMapper.toDetailDto(pkg);
    }

    // ================================================================
    // UPDATE
    // ================================================================

    @Override
    @Transactional
    public PackageDetailDTO updatePackage(Long id, PackageRequestDTO request) {
        log.info("Updating package with id: {}", id);

        // Step 1: Find existing package
        Package pkg = packageRepository.findById(id)
            .orElseThrow(() -> new AppException("Package not found: " + id, 404));

        // Step 2: Check tracking number conflict
        packageRepository.findByTrackingNumber(request.trackingNumber())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new AppException("Tracking number already exists: " + request.trackingNumber(), 409);
                }
            });

        // Step 3: Update fields
        pkg.setTrackingNumber(request.trackingNumber());
        pkg.setTotalWeightKg(request.totalWeightKg());
        pkg.setTotalVolumeCbm(request.totalVolumeCbm());

        // Step 4: Update store if changed
        if (!pkg.getStore().getId().equals(request.storeId())) {
            Store store = storeRepository.findById(request.storeId())
                .orElseThrow(() -> new AppException("Store not found: " + request.storeId(), 404));
            pkg.setStore(store);
        }

        // Step 5: Save and return
        Package updated = packageRepository.save(pkg);
        log.info("Package updated with id: {}", updated.getId());

        return getPackageDetail(updated.getId());
    }

    // ================================================================
    // DELETE
    // ================================================================

    @Override
    @Transactional
    public void deletePackage(Long id) {
        log.info("Deleting package with id: {}", id);
        Package pkg = packageRepository.findById(id)
            .orElseThrow(() -> new AppException("Package not found: " + id, 404));
        packageRepository.delete(pkg);
        log.info("Package deleted with id: {}", id);
    }

    // ================================================================
    // STATUS MANAGEMENT
    // ================================================================

    @Override
    @Transactional
    public void updatePackageStatus(List<Long> packageIds, String status) {
        log.info("Updating {} packages to status: {}", packageIds.size(), status);

        // STEP 1: Validate status
        PackageStatus newStatus = validateAndParseStatus(status);

        // STEP 2: Validate packages exist
        List<Package> packages = validatePackagesExist(packageIds);

        // STEP 3: Validate business rules
        validateStatusTransition(packages, newStatus);

        // STEP 4: Update
        for (Package pkg : packages) {
            pkg.setStatus(newStatus);
        }
        packageRepository.saveAll(packages);
        log.info("Updated {} packages to status: {}", packages.size(), newStatus);
    }

    // ================================================================
    // PRIVATE VALIDATION METHODS
    // ================================================================

    /**
     * Validates and parses the status string to PackageStatus enum.
     * 
     * @param status The status string from the request
     * @return PackageStatus enum value
     * @throws AppException if status is invalid
     */
    private PackageStatus validateAndParseStatus(String status) {
        try {
            return PackageStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new AppException(
                "Invalid status: " + status + ". Valid values: " +
                    String.join(", ", PackageStatus.getAllNames()),
                400
            );
        }
    }

    /**
     * Validates that all packages exist.
     * 
     * @param packageIds List of package IDs
     * @return List of found packages
     * @throws AppException if any package not found
     */
    private List<Package> validatePackagesExist(List<Long> packageIds) {
        List<Package> packages = packageRepository.findAllById(packageIds);
        
        if (packages.size() != packageIds.size()) {
            List<Long> foundIds = packages.stream().map(Package::getId).toList();
            List<Long> missingIds = packageIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
            throw new AppException("Packages not found: " + missingIds, 404);
        }
        return packages;
    }

    /**
     * Validates business rules for status transitions.
     * 
     * Business Rules:
     * - Only READY_FOR_PICKUP packages can transition to IN_TRANSIT
     * - Only IN_TRANSIT packages can transition to DELIVERED
     * - Only CREATED/PROCESSING packages can transition to CANCELLED
     * 
     * @param packages List of packages to validate
     * @param newStatus The new status
     * @throws AppException if validation fails
     */
    private void validateStatusTransition(List<Package> packages, PackageStatus newStatus) {
        for (Package pkg : packages) {
            PackageStatus current = pkg.getStatus();
            
            // Only READY_FOR_PICKUP → IN_TRANSIT
            if (newStatus == PackageStatus.IN_TRANSIT && current != PackageStatus.READY_FOR_PICKUP) {
                throw new AppException(
                    String.format("Package %d has status '%s' but must be 'READY_FOR_PICKUP' to transition to IN_TRANSIT",
                        pkg.getId(), current),
                    400
                );
            }
            
            // Only IN_TRANSIT → DELIVERED
            if (newStatus == PackageStatus.DELIVERED && current != PackageStatus.IN_TRANSIT) {
                throw new AppException(
                    String.format("Package %d has status '%s' but must be 'IN_TRANSIT' to transition to DELIVERED",
                        pkg.getId(), current),
                    400
                );
            }
            
            // Only CREATED or PROCESSING → CANCELLED
            if (newStatus == PackageStatus.CANCELLED && 
                current != PackageStatus.CREATED && 
                current != PackageStatus.PROCESSING) {
                throw new AppException(
                    String.format("Package %d has status '%s' but must be 'CREATED' or 'PROCESSING' to transition to CANCELLED",
                        pkg.getId(), current),
                    400
                );
            }
        }
    }
}