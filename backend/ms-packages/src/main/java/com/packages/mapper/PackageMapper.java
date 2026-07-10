package com.packages.mapper;

import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.response.PackageDetailDTO;
import com.packages.dto.response.PackageResponseDTO;
import com.packages.dto.response.StoreResponseDTO;
import com.packages.dto.external.PackageDTO;
import com.packages.model.entity.Package;
import com.packages.model.entity.Store;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PackageMapper {

    // ================================================================
    // ENTITY → DTO
    // ================================================================

    /**
     * Maps Package entity to PackageDTO (for external communication).
     */
    PackageDTO toPackageDto(Package entity);

    /**
     * Maps Package entity to PackageResponseDTO (basic list view).
     */
    @Mapping(source = "store.id", target = "storeId")
    PackageResponseDTO toResponseDto(Package entity);

    /**
     * Maps Package entity to PackageDetailDTO (detailed view with store).
     * This requires the store to be loaded (JOIN FETCH).
     */
    @Mapping(source = "store", target = "store")
    PackageDetailDTO toDetailDto(Package entity);

    /**
     * Maps Store entity to StoreResponseDTO.
     */
    StoreResponseDTO toStoreResponseDto(Store store);

    // ================================================================
    // DTO → ENTITY
    // ================================================================

    /**
     * Maps PackageRequestDTO to Package entity.
     * The store ID is not mapped here; it's set in the service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "ownerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Package toEntity(PackageRequestDTO dto);

    // ================================================================
    // COLLECTION MAPPINGS
    // ================================================================

    List<PackageResponseDTO> toResponseDtoList(List<Package> entities);
    List<PackageDetailDTO> toDetailDtoList(List<Package> entities);
    List<PackageDTO> toPackageDtoList(List<Package> entities);
}