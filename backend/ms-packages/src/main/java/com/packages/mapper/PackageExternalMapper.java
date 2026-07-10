package com.packages.mapper;

import com.packages.dto.external.LocationDTO;
import com.packages.dto.external.PackageDTO;
import com.packages.model.embedded.Location;
import com.packages.model.entity.Package;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface PackageExternalMapper {

    /**
     * Maps Package entity to PackageDTO for ms-routes communication.
     * Requires store to be loaded (JOIN FETCH).
     */
    @Mapping(source = "store.location", target = "origin")
    @Mapping(target = "id", source = "id")
    @Mapping(target = "totalWeightKg", source = "totalWeightKg")
    @Mapping(target = "totalVolumeCbm", source = "totalVolumeCbm")
    PackageDTO toPackageDto(Package entity);

    /**
     * Maps Location embeddable to LocationDTO.
     */
    LocationDTO toLocationDto(Location location);

    /**
     * Maps a list of Package entities to a list of PackageDTOs.
     * This is automatically implemented by MapStruct.
     */
    List<PackageDTO> toPackageDtoList(List<Package> entities);
}