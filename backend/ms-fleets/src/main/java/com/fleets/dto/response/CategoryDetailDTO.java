package com.fleets.dto.response;

/**
 * DTO for Category response.
 * Independent from Vehicle DTO.
 */
public record CategoryDetailDTO(
    Long id,
    String name,
    String description,
    boolean isActive
) {
    
}