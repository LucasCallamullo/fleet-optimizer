package com.packages.service;

import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.response.PackageDetailDTO;
import com.packages.dto.response.PackageResponseDTO;

import java.util.List;

public interface PackageService {

    // ================================================================
    // CREATE
    // ================================================================

    PackageDetailDTO createPackage(PackageRequestDTO request, String ownerId);

    // ================================================================
    // READ
    // ================================================================

    List<PackageResponseDTO> getAllPackages();

    List<PackageDetailDTO> getAllPackagesWithStore();

    PackageResponseDTO getPackage(Long id);

    PackageDetailDTO getPackageDetail(Long id);

    // ================================================================
    // UPDATE
    // ================================================================

    PackageDetailDTO updatePackage(Long id, PackageRequestDTO request);

    // ================================================================
    // DELETE
    // ================================================================

    void deletePackage(Long id);
}