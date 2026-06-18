package com.fleets.dto.request;

import jakarta.validation.constraints.*;

/**
 * DTO for creating and updating Category entities.
 * Contains validation annotations to ensure data integrity.
 */
public record CategoryRequestDTO(
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 32, message = "Category name must be between 2 and 32 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Category name must contain only letters and spaces")
    String name,

    Boolean isActive // ​​We use Boolean (Object) to detect if null is present
) {
    // Compact constructor to apply default logic
    public CategoryRequestDTO {
        if (isActive == null) {
            isActive = true; // If the JSON didn't contain null, it becomes true
        }
    }
}