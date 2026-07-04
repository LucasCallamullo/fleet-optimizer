package com.fleets.mapper;

import java.util.List; 

// IMPORTS - MapStruct Annotations
import org.mapstruct.Mapper;           // Core annotation: marks this as a MapStruct mapper
import org.mapstruct.Mapping;          // Configures field mappings between source and target
import org.mapstruct.MappingTarget;    // For updating existing entities (PATCH operations)
import org.mapstruct.ReportingPolicy;  // Configures how to handle unmapped fields
import org.mapstruct.MappingConstants; // Contains constants like SPRING component model
// import org.mapstruct.Named;            // For named mappings (multiple mapping methods)
// import org.mapstruct.Context;          // For passing context objects (like Locale)

// IMPORTS - Project DTOs
import com.fleets.dto.request.VehicleRequestDTO;   // Input DTO (create/update)
import com.fleets.dto.response.VehicleResponseDTO;  // Output DTO (basic vehicle info)

import com.fleets.dto.response.VehicleDetailDTO;    // Output DTO (detailed with category)
import com.fleets.model.Vehicle;                    // JPA Entity

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {CategoryMapper.class},
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface VehicleMapper {
    
    /**
     * DETAILED MAPPING - Entity → Detail DTO (READ WITH CATEGORY)
     * 
     * @description Converts Vehicle entity to VehicleDetailDTO with
     *              category information flattened (not nested).
     * 
     * @param entity - The Vehicle entity from the database
     * @return VehicleDetailDTO - Detailed DTO with category info
     * 
     * @Mapping CONFIGURATION:
     * 
     * @Mapping(target = "categoryName", source = "category.name")
     *   → Maps: entity.getCategory().getName() → dto.categoryName
     *   → Flattens nested object into a simple field
     * 
     * @Mapping(target = "categoryId", source = "category.id")
     *   → Maps: entity.getCategory().getId() → dto.categoryId
     *   → Flattens nested object into a simple field
     * 
     */
    @Mapping(target = "category", source = "category")
    VehicleDetailDTO toDetailDto(Vehicle entity);

    /**
     * BASIC MAPPING - DTO → Entity (CREATE)
     * 
     * @description Converts VehicleRequestDTO to a new Vehicle entity.
     *              Used when creating a new vehicle.
     * 
     * @param dto - The DTO containing input data from the client
     * @return Vehicle - New entity ready for persistence
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)  // setting on service
    @Mapping(target = "createdAt", ignore = true) // ← @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // ← @UpdateTimestamp
    Vehicle toEntity(VehicleRequestDTO dto);
    
    /**
     * BASIC MAPPING - Entity → DTO (READ)
     * 
     * @description Converts Vehicle entity to VehicleResponseDTO.
     *              Used for standard GET operations.
     * 
     * @param entity - The Vehicle entity from the database
     * @return VehicleResponseDTO - DTO to send back to client
     */
    // For listings - ID only (no JOIN FETCH)
    @Mapping(target = "categoryId", source = "category.id")
    VehicleResponseDTO toDto(Vehicle entity);
    
    /**
     * UPDATE MAPPING - DTO → Existing Entity (PATCH)
     * 
     * @description Updates an existing Vehicle entity with data from DTO.
     *              Used for PATCH/PUT operations (partial/full updates).
     * 
     * @param dto - The DTO with update data
     * @param entity - The existing entity to update (from database)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true) // Service layer handles category resolution
    @Mapping(target = "createdAt", ignore = true) // ← @CreationTimestamp
    @Mapping(target = "updatedAt", ignore = true) // ← @UpdateTimestamp
    void updateEntity(
        @MappingTarget Vehicle entity,  // The entity to update
        VehicleRequestDTO dto           // The source data
    );
    
    /**
     * COLLECTION MAPPING - List<Entity> → List<DTO>
     * 
     * @description Converts a list of Vehicle entities to a list of
     *              VehicleResponseDTOs.
     * 
     * @param entities - List of Vehicle entities from the database
     * @return List<VehicleResponseDTO> - List of DTOs to send to client
     */
    List<VehicleResponseDTO> toDtoList(List<Vehicle> entities);
    
    /**
     * ================================================================
     * 6. ADVANCED MAPPING - Named Mappings (Optional)
     * ================================================================
     * 
     * @description Example of using @Named for multiple mapping strategies.
     *              Useful when you need different mappings for different use cases.
     * 
     * @param entity - The Vehicle entity
     * @return VehicleResponseDTO - Mapped DTO with status
     * 
     * USAGE:
     * // Define named mapping method
     * @Named("withStatus")
     * @Mapping(target = "status", expression = "java(mapStatus(entity))")
     * VehicleResponseDTO toDtoWithStatus(Vehicle entity);
     * 
     * // Reference in another mapper
     * @Mapping(target = "vehicle", qualifiedByName = "withStatus")
     * ReservationDTO toDto(Reservation reservation);
     * 
     * NOTE: Commented out as optional - uncomment if needed
     */
    // @Named("withStatus")
    // @Mapping(target = "status", expression = "java(mapStatus(entity))")
    // VehicleResponseDTO toDtoWithStatus(Vehicle entity);
    
    /**
     * ================================================================
     * 7. HELPER METHODS - Custom Logic (Optional)
     * ================================================================
     * 
     * @description Helper methods for complex mapping logic.
     *              Can be used in @Mapping(expression = "java(...)").
     * 
     * @param entity - The Vehicle entity
     * @return String - Mapped status
     * 
     * NOTE: Commented out as optional - uncomment if needed
     */
    // default String mapStatus(Vehicle entity) {
    //     if (entity == null) return "UNKNOWN";
    //     return entity.isActive() ? "ACTIVE" : "INACTIVE";
    // }
}
