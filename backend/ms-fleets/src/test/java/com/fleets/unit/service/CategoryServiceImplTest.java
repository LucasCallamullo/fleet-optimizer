package com.fleets.unit.service;

import com.fleets.dto.request.CategoryRequestDTO;
import com.fleets.dto.response.CategoryDetailDTO;
import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.exception.AppException;
import com.fleets.mapper.CategoryMapper;
import com.fleets.model.Category;
import com.fleets.repository.CategoryRepository;
import com.fleets.service.impl.CategoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Category Service Unit Tests")
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper mapper;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private Category category;
    private Category savedCategory;
    private CategoryRequestDTO request;
    private CategoryResponseDTO responseDTO;
    private CategoryDetailDTO detailDTO;

    @BeforeEach
    void setUp() {
        // Step 1: Create category entity
        category = new Category();
        category.setId(1L);
        category.setName("Sedan");
        category.setDescription("Luxury sedan category");
        category.setActive(true);

        // Step 2: Create saved category (same as category but with ID)
        savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Sedan");
        savedCategory.setDescription("Luxury sedan category");
        savedCategory.setActive(true);

        // Step 3: Create request DTO
        request = new CategoryRequestDTO(
            "Sedan",
            "Luxury sedan category",
            true
        );

        // Step 4: Create response DTO
        responseDTO = new CategoryResponseDTO(
            1L,
            "Sedan",
            "Some description"
        );

        // Step 5: Create detail DTO
        detailDTO = new CategoryDetailDTO(
            1L,
            "Sedan",
            "Luxury sedan category",
            true
        );
    }

    // ================================================================
    // TEST: ENTITY METHODS
    // ================================================================

    @Test
    @DisplayName("Should return all categories as entities")
    void shouldReturnAllCategoriesEntity() {
        // Step 1: Arrange
        List<Category> categories = List.of(category);
        when(categoryRepository.findAll()).thenReturn(categories);

        // Step 2: Act
        List<Category> result = categoryService.getAllCategoriesEntity();

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Should return category entity by ID when it exists")
    void shouldReturnCategoryEntityById() {
        // Step 1: Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Step 2: Act
        Category result = categoryService.getCategoryEntityById(categoryId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findById(categoryId);
    }

    @Test
    @DisplayName("Should throw exception when category entity not found by ID")
    void shouldThrowExceptionWhenCategoryEntityNotFound() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryEntityById(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Category not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(categoryRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("Should return category entity by name when it exists")
    void shouldReturnCategoryEntityByName() {
        // Step 1: Arrange
        String name = "Sedan";
        when(categoryRepository.findByName(name)).thenReturn(Optional.of(category));

        // Step 2: Act
        Category result = categoryService.getCategoryEntityByName(name);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findByName(name);
    }

    @Test
    @DisplayName("Should throw exception when category entity not found by name")
    void shouldThrowExceptionWhenCategoryEntityNotFoundByName() {
        // Step 1: Arrange
        String nonExistentName = "NonExistent";
        when(categoryRepository.findByName(nonExistentName)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryEntityByName(nonExistentName))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Category not found with name: NonExistent")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(categoryRepository).findByName(nonExistentName);
    }

    @Test
    @DisplayName("Should return Optional of category by name")
    void shouldReturnOptionalCategoryByName() {
        // Step 1: Arrange
        String name = "Sedan";
        when(categoryRepository.findByName(name)).thenReturn(Optional.of(category));

        // Step 2: Act
        Optional<Category> result = categoryService.findCategoryEntityByName(name);

        // Step 3: Assert
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findByName(name);
    }

    @Test
    @DisplayName("Should return empty Optional when category not found by name")
    void shouldReturnEmptyOptionalWhenCategoryNotFoundByName() {
        // Step 1: Arrange
        String nonExistentName = "NonExistent";
        when(categoryRepository.findByName(nonExistentName)).thenReturn(Optional.empty());

        // Step 2: Act
        Optional<Category> result = categoryService.findCategoryEntityByName(nonExistentName);

        // Step 3: Assert
        assertThat(result).isEmpty();

        // Step 4: Verify
        verify(categoryRepository).findByName(nonExistentName);
    }

    // ================================================================
    // TEST: DTO METHODS
    // ================================================================

    @Test
    @DisplayName("Should return all categories as DTOs")
    void shouldReturnAllCategoriesAsDTOs() {
        // Step 1: Arrange
        List<Category> categories = List.of(category);
        List<CategoryResponseDTO> dtos = List.of(responseDTO);

        when(categoryRepository.findAll()).thenReturn(categories);
        when(mapper.toDtoList(categories)).thenReturn(dtos);

        // Step 2: Act
        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findAll();
        verify(mapper).toDtoList(categories);
    }

    @Test
    @DisplayName("Should return category detail DTO by ID")
    void shouldReturnCategoryDetailById() {
        // Step 1: Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(mapper.toDetailDto(category)).thenReturn(detailDTO);

        // Step 2: Act
        CategoryDetailDTO result = categoryService.getCategoryById(categoryId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Sedan");
        assertThat(result.description()).isEqualTo("Luxury sedan category");
        assertThat(result.isActive()).isTrue();

        // Step 4: Verify
        verify(categoryRepository).findById(categoryId);
        verify(mapper).toDetailDto(category);
    }

    @Test
    @DisplayName("Should return category detail DTO by name")
    void shouldReturnCategoryDetailByName() {
        // Step 1: Arrange
        String name = "Sedan";
        when(categoryRepository.findByName(name)).thenReturn(Optional.of(category));
        when(mapper.toDetailDto(category)).thenReturn(detailDTO);

        // Step 2: Act
        CategoryDetailDTO result = categoryService.getCategoryByName(name);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findByName(name);
        verify(mapper).toDetailDto(category);
    }

    // ================================================================
    // TEST: CREATE CATEGORY
    // ================================================================

    @Test
    @DisplayName("Should create category successfully")
    void shouldCreateCategory() {
        // Step 1: Arrange
        when(categoryRepository.existsByName(request.name())).thenReturn(false);
        when(mapper.toEntityFromDetail(request)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(mapper.toDetailDto(savedCategory)).thenReturn(detailDTO);

        // Step 2: Act
        CategoryDetailDTO result = categoryService.createCategory(request);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Sedan");
        assertThat(result.isActive()).isTrue();

        // Step 4: Verify
        verify(categoryRepository).existsByName(request.name());
        verify(mapper).toEntityFromDetail(request);
        verify(categoryRepository).save(any(Category.class));
        verify(mapper).toDetailDto(savedCategory);
    }

    @Test
    @DisplayName("Should throw exception when creating category with duplicate name")
    void shouldThrowExceptionWhenCreatingDuplicateCategory() {
        // Step 1: Arrange
        when(categoryRepository.existsByName(request.name())).thenReturn(true);

        // Step 2: Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Category name already exists: Sedan")
            .hasFieldOrPropertyWithValue("statusCode", 409);

        // Step 3: Verify
        verify(categoryRepository).existsByName(request.name());
        verify(categoryRepository, never()).save(any(Category.class));
        verify(mapper, never()).toDetailDto(any(Category.class));
    }

    // ================================================================
    // TEST: UPDATE CATEGORY
    // ================================================================

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategory() {
        // Step 1: Arrange
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old Name");
        existingCategory.setActive(true);

        Category updatedCategory = new Category();
        updatedCategory.setId(categoryId);
        updatedCategory.setName("Sedan");
        updatedCategory.setActive(true);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName(request.name())).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenReturn(updatedCategory);
        when(mapper.toDetailDto(updatedCategory)).thenReturn(detailDTO);

        // Step 2: Act
        CategoryDetailDTO result = categoryService.updateCategory(categoryId, request);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Sedan");

        // Step 4: Verify
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByName(request.name());
        verify(categoryRepository).save(any(Category.class));
        verify(mapper).toDetailDto(updatedCategory);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent category")
    void shouldThrowExceptionWhenUpdatingNonExistentCategory() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(nonExistentId, request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Category not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(categoryRepository).findById(nonExistentId);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    @DisplayName("Should throw exception when updating to duplicate name on another category")
    void shouldThrowExceptionWhenUpdatingToDuplicateName() {
        // Step 1: Arrange
        Long categoryId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(categoryId);
        existingCategory.setName("Old Name");

        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Sedan");

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.existsByName("Sedan")).thenReturn(true);
        when(categoryRepository.findByName("Sedan")).thenReturn(Optional.of(otherCategory));

        // Step 2: Act & Assert
        assertThatThrownBy(() -> categoryService.updateCategory(categoryId, request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Category name 'Sedan' is already used by another category")
            .hasFieldOrPropertyWithValue("statusCode", 409);

        // Step 3: Verify
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).existsByName("Sedan");
        verify(categoryRepository).findByName("Sedan");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // ================================================================
    // TEST: DELETE CATEGORY
    // ================================================================

    @Test
    @DisplayName("Should delete category successfully")
    void shouldDeleteCategory() {
        // Step 1: Arrange
        Long categoryId = 1L;
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        // Step 2: Act
        categoryService.deleteCategory(categoryId);

        // Step 3: Verify
        verify(categoryRepository).findById(categoryId);
        verify(categoryRepository).delete(category);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent category")
    void shouldThrowExceptionWhenDeletingNonExistentCategory() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Category not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(categoryRepository).findById(nonExistentId);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    // ================================================================
    // TEST: UTILITY METHODS
    // ================================================================

    @Test
    @DisplayName("Should return true when category exists by name")
    void shouldReturnTrueWhenCategoryExistsByName() {
        // Step 1: Arrange
        String name = "Sedan";
        when(categoryRepository.existsByName(name)).thenReturn(true);

        // Step 2: Act
        boolean result = categoryService.existsByName(name);

        // Step 3: Assert
        assertThat(result).isTrue();

        // Step 4: Verify
        verify(categoryRepository).existsByName(name);
    }

    @Test
    @DisplayName("Should return false when category does not exist by name")
    void shouldReturnFalseWhenCategoryDoesNotExistByName() {
        // Step 1: Arrange
        String name = "NonExistent";
        when(categoryRepository.existsByName(name)).thenReturn(false);

        // Step 2: Act
        boolean result = categoryService.existsByName(name);

        // Step 3: Assert
        assertThat(result).isFalse();

        // Step 4: Verify
        verify(categoryRepository).existsByName(name);
    }
}