package com.fleets.dto.response;

import com.fleets.model.Category;

/**
 * DTO for Category response.
 * Independent from Vehicle DTO.
 */
public record CategoryResponseDTO(
    Long id,
    String name
) {
    /**
     * Converts a Category entity to CategoryResponseDTO
     * @param category the category entity
     * @return the response DTO, or null if category is null
     */
    public static CategoryResponseDTO fromEntity(Category category) {
        if (category == null) return null;
        return new CategoryResponseDTO(category.getId(), category.getName());
    }
}