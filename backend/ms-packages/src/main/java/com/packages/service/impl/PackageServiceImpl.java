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

        // Step 5: Return detail with store loaded
        return getPackageDetail(saved.getId());
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
}