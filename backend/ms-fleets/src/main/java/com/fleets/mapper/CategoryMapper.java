package com.fleets.mapper;

import java.util.List; 

import org.mapstruct.Mapper;           // Core annotation: marks this as a MapStruct mapper
import org.mapstruct.Mapping;          // Configures field mappings between source and target
import org.mapstruct.ReportingPolicy;  // Configures how to handle unmapped fields
import org.mapstruct.MappingConstants; // Contains constants like SPRING component model
import org.mapstruct.MappingTarget;

import com.fleets.dto.response.CategoryResponseDTO;   // Basic DTO (id + name)
import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.dto.response.CategoryDetailDTO;     // Detailed DTO (all fields)
import com.fleets.model.Category;                     // JPA Entity

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CategoryMapper {
    
    /**
     * BASIC MAPPING - Entity → DTO (READ)
     * 
     * @description Converts Category entity to CategoryDTO.
     *              Used for basic category information.
     * 
     * @param entity - The Category entity from the database
     * @return CategoryDTO - Basic DTO with id and name
     */
    CategoryResponseDTO toDto(Category entity);
    
    /**
     * DETAILED MAPPING - Entity → Detail DTO
     * 
     * @description Converts Category entity to CategoryDetailDTO
     *              with all category information.
     * 
     * @param entity - The Category entity from the database
     * @return CategoryDetailDTO - Detailed DTO with all fields
     */
    @Mapping(target = "isActive", source = "active")     // Rename: active → isActive
    CategoryDetailDTO toDetailDto(Category entity);
    
    /**
     * BASIC MAPPING - DTO → Entity (CREATE)
     * 
     * @description Converts CategoryDTO to a new Category entity.
     *              Used when creating a new category.
     * 
     * @param dto - The DTO containing input data from the client
     * @return Category - New entity ready for persistence
     */
    Category toEntity(CategoryRequestDTO dto);
    
    /**
     * DETAILED MAPPING - DTO → Entity (CREATE WITH DETAILS)
     * 
     * @description Converts CategoryDetailDTO to a new Category entity.
     *              Used when creating a category with all fields.
     * 
     * @param dto - The DTO containing all category data
     * @return Category - New entity ready for persistence
     */
    @Mapping(target = "active", source = "dto.isActive")          // Rename: isActive → active
    Category toEntityFromDetail(CategoryRequestDTO dto);
    
    /**
     * UPDATE MAPPING - DTO → Existing Entity (PATCH)
     * 
     * @description Updates an existing Category entity with data from DTO.
     *              Used for PATCH/PUT operations.
     * 
     * @param dto - The DTO with update data
     * @param entity - The existing entity to update (from database)
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", source = "dto.isActive") // Rename: isActive → active
    void updateEntity(
        @MappingTarget Category entity, // The entity to update
        CategoryRequestDTO dto           // The source data
    );
    
    /**
     * COLLECTION MAPPING - List<Entity> → List<DTO>
     * 
     * @description Converts a list of Category entities to a list of
     *              CategoryDTOs.
     * 
     * @param entities - List of Category entities from the database
     * @return List<CategoryDTO> - List of DTOs to send to client
     */
    List<CategoryResponseDTO> toDtoList(List<Category> entities);
    
    /**
     * COLLECTION MAPPING - List<Entity> → List<Detail DTO>
     * 
     * @description Converts a list of Category entities to a list of
     *              CategoryDetailDTOs.
     * 
     * @param entities - List of Category entities from the database
     * @return List<CategoryDetailDTO> - List of detailed DTOs
     */
    List<CategoryDetailDTO> toDetailDtoList(List<Category> entities);
    
    /**
     * HELPER METHOD - ID to Entity (for reference)
     * 
     * @description Creates a Category entity with just the ID.
     *              Useful when you only have the ID and want to set
     *              the relation without fetching the entire entity.
     * 
     * @param id - The category ID
     * @return Category - Entity with ID set (other fields null)
     */
    default Category idToEntity(Long id) {
        if (id == null) {
            return null;
        }
        Category category = new Category();
        category.setId(id);
        return category;
    }
}
