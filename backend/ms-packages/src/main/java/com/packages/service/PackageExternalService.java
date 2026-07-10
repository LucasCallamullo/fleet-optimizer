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

@Service
@Slf4j
@RequiredArgsConstructor
public class PackageExternalService {

    private final PackageRepository packageRepository;
    private final PackageExternalMapper packageExternalMapper;

    /**
     * Fetches packages by IDs and maps them to PackageDTOs.
     * Uses JOIN FETCH to ensure store is loaded.
     */
    public List<PackageDTO> getPackagesDto(List<Long> ids) {
        log.debug("Fetching package DTOs for ids: {}", ids);

        // Step 1: Fetch packages with store eagerly loaded
        List<Package> packages = packageRepository.findAllByIdWithStore(ids);

        // Step 2: Map to DTOs (store is already loaded)
        return packageExternalMapper.toPackageDtoList(packages);
    }

    /**
     * Fetches a single package by ID and maps to PackageDTO.
     */
    public PackageDTO getPackageDto(Long id) {
        log.debug("Fetching package DTO for id: {}", id);

        // Step 1: Fetch package with store eagerly loaded
        Package pkg = packageRepository.findByIdWithStore(id)
            .orElseThrow(() -> new AppException("Package not found: " + id, 404));

        // Step 2: Map to DTO (store is already loaded)
        return packageExternalMapper.toPackageDto(pkg);
    }
}