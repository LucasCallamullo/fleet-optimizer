package com.fleets.dto.request;

import com.fleets.model.Category;

import jakarta.validation.constraints.*;
import lombok.Data;

/**
 * DTO for creating and updating Category entities.
 * Contains validation annotations to ensure data integrity.
 */
@Data
public class CategoryRequestDTO {
    
    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 32, message = "Category name must be between 2 and 32 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Category name must contain only letters and spaces")
    private String name;
    
    /**
     * Converts this DTO to a Category entity.
     * 
     * @return a new Category entity with name populated
     */
    public Category toEntity() {
        Category category = new Category();
        category.setName(this.name);
        return category;
    }
}