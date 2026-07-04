package com.routes.mapper;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.LegDetailDTO;
import com.routes.dto.response.LocationResponseDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Location;
import com.routes.model.entity.Route;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface RouteMapper {
    
    // ================================================================
    // ENTITY → DTO
    // ================================================================
    
    /**
     * Converts Route entity to RouteDetailDTO.
     * MapStruct automatically maps:
     * - route.legs → dto.legs (calls toLegDetailDto for each)
     * - route.legs[].origin → dto.legs[].origin (auto-mapped)
     * - route.legs[].destination → dto.legs[].destination (auto-mapped)
     */
    RouteDetailDTO toDetailDto(Route route);
    
    /**
     * Converts Leg entity to LegDetailDTO.
     * MapStruct automatically maps:
     * - leg.origin → dto.origin (Location → LocationResponseDTO)
     * - leg.destination → dto.destination (Location → LocationResponseDTO)
     */
    LegDetailDTO toLegDetailDto(Leg leg);
    
    /**
     * Converts Location embeddable to LocationResponseDTO.
     * All fields have the same name, so MapStruct maps them automatically.
     */
    LocationResponseDTO toLocationResponseDto(Location location);
    
    // ================================================================
    // DTO → ENTITY
    // ================================================================
    
    /**
     * Converts RouteRequestDTO to Route entity.
     * Note: legs are NOT mapped here (handled separately to set bidirectional).
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "legs", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Route toEntity(RouteRequestDTO dto);
    
    /**
     * Converts LegRequestDTO to Leg entity.
     * MapStruct automatically maps:
     * - dto.origin → leg.origin (LocationRequestDTO → Location)
     * - dto.destination → leg.destination (LocationRequestDTO → Location)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "route", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "distanceKm", ignore = true)
    @Mapping(target = "durationMinutes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    Leg toLegEntity(LegRequestDTO dto);
    
    /**
     * Converts LocationRequestDTO to Location embeddable.
     * All fields have the same name, so MapStruct maps them automatically.
     */
    Location toLocationEntity(LocationRequestDTO dto);
    
    // ================================================================
    // COLLECTION MAPPINGS
    // ================================================================
    
    List<LegDetailDTO> toLegDetailDtoList(List<Leg> legs);
}