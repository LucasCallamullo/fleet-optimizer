package com.fleets.dto.response;

/**
 * DTO for Category response.
 * Independent from Vehicle DTO.
 */
public record CategoryResponseDTO(
    Long id,
    String name,
    String description
) {
    
}