package com.routes.mapper;

import com.routes.dto.client.common.LocationDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.model.entity.Location;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Location conversions between DTOs and Entity.
 * 
 * This mapper handles all Location conversions used in the shipment flow:
 * - LocationRequestDTO (API input) → Location (Embeddable)
 * - LocationDTO (inter-service) → Location (Embeddable)
 * - Location (Embeddable) → LocationDTO (inter-service)
 * 
 * The Location class is an @Embeddable, not an entity. This does NOT affect
 * the mapping behavior - MapStruct works the same way for embeddables.
 * 
 * Configuration:
 * - componentModel = SPRING → Generates a Spring bean (can be injected with @Autowired)
 * - unmappedTargetPolicy = IGNORE → Ignores unmapped fields without warnings
 */
@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface LocationMapper {

    /**
     * Maps LocationRequestDTO (API input) to Location entity.
     * 
     * @param dto The API request DTO with location data
     * @return Location entity
     */
    Location toEntity(LocationRequestDTO dto);

    /**
     * Maps LocationDTO (inter-service) to Location entity.
     * 
     * @param dto The inter-service DTO with location data
     * @return Location entity
     */
    Location toEntity(LocationDTO dto);

    /**
     * Maps Location entity to LocationDTO (inter-service).
     * 
     * @param location The Location entity
     * @return LocationDTO for inter-service communication
     */
    LocationDTO toDto(Location location);
}