package com.routes.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * DTO for creating a complete route with its legs.
 */
public record RouteRequestDTO(
    
    @NotBlank(message = "Route name is required")
    @Size(min = 3, max = 100, message = "Route name must be between 3 and 100 characters")
    String name,
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    String description,
    
    @NotEmpty(message = "At least one leg is required")
    @Valid
    List<LegRequestDTO> legs
) {}