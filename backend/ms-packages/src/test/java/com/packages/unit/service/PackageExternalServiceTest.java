package com.packages.unit.service;

import com.packages.dto.external.LocationDTO;
import com.packages.dto.external.PackageDTO;
import com.packages.exception.AppException;
import com.packages.mapper.PackageExternalMapper;
import com.packages.model.embedded.Location;
import com.packages.model.entity.Package;
import com.packages.model.entity.Store;
import com.packages.repository.PackageRepository;
import com.packages.service.PackageExternalService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Package External Service Unit Tests")
class PackageExternalServiceTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private PackageExternalMapper packageExternalMapper;

    @InjectMocks
    private PackageExternalService packageExternalService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private Package pkg;
    private Store store;
    private Location location;
    private PackageDTO packageDTO;

    @BeforeEach
    void setUp() {
        // Step 1: Create location
        location = new Location();
        location.setStreet("Av. Libertador");
        location.setStreetNumber("1000");
        location.setCity("Buenos Aires");
        location.setCountry("Argentina");
        location.setLatitude(-34.6037);
        location.setLongitude(-58.3816);

        // Step 2: Create store
        store = new Store();
        store.setId(1L);
        store.setName("Downtown Warehouse");
        store.setLocation(location);

        // Step 3: Create package
        pkg = new Package();
        pkg.setId(1L);
        pkg.setTrackingNumber("PKG-001");
        pkg.setTotalWeightKg(10.0);
        pkg.setTotalVolumeCbm(0.30);
        pkg.setStore(store);
        pkg.setOwnerId("user-id");

        // Step 4: Create LocationDTO
        LocationDTO originDTO = new LocationDTO(
            "Av. Libertador",
            "1000",
            "Buenos Aires",
            "CABA",
            "Argentina",
            "1000",
            -34.6037,
            -58.3816
        );

        // Step 5: Create PackageDTO
        packageDTO = new PackageDTO(
            1L,
            10.0,
            0.30,
            originDTO
        );
    }

    // ================================================================
    // TEST: GET PACKAGES DTO - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should return PackageDTOs for multiple IDs")
    void shouldReturnPackageDTOsForMultipleIds() {
        // Step 1: Arrange
        List<Long> ids = List.of(1L, 2L, 3L);
        List<Package> packages = List.of(pkg);

        when(packageRepository.findAllByIdWithStore(ids)).thenReturn(packages);
        when(packageExternalMapper.toPackageDtoList(packages)).thenReturn(List.of(packageDTO));

        // Step 2: Act
        List<PackageDTO> result = packageExternalService.getPackagesDto(ids);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).totalWeightKg()).isEqualTo(10.0);
        assertThat(result.get(0).totalVolumeCbm()).isEqualTo(0.30);
        assertThat(result.get(0).origin()).isNotNull();
        assertThat(result.get(0).origin().city()).isEqualTo("Buenos Aires");

        // Step 4: Verify
        verify(packageRepository).findAllByIdWithStore(ids);
        verify(packageExternalMapper).toPackageDtoList(packages);
    }

    // ================================================================
    // TEST: GET PACKAGES DTO - EMPTY LIST
    // ================================================================

    @Test
    @DisplayName("Should return empty list when no packages found")
    void shouldReturnEmptyListWhenNoPackagesFound() {
        // Step 1: Arrange
        List<Long> ids = List.of(99L, 100L);

        when(packageRepository.findAllByIdWithStore(ids)).thenReturn(List.of());

        // Step 2: Act
        List<PackageDTO> result = packageExternalService.getPackagesDto(ids);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Step 4: Verify
        verify(packageRepository).findAllByIdWithStore(ids);
        verify(packageExternalMapper, never()).toPackageDtoList(any());
    }

    // ================================================================
    // TEST: GET PACKAGES DTO - NULL OR EMPTY IDS
    // ================================================================

    @Test
    @DisplayName("Should return empty list when IDs are null or empty")
    void shouldReturnEmptyListWhenIdsAreNullOrEmpty() {
        // Step 1: Act with null
        List<PackageDTO> resultNull = packageExternalService.getPackagesDto(null);

        // Step 2: Assert
        assertThat(resultNull).isNotNull();
        assertThat(resultNull).isEmpty();

        // Step 3: Act with empty
        List<PackageDTO> resultEmpty = packageExternalService.getPackagesDto(List.of());

        // Step 4: Assert
        assertThat(resultEmpty).isNotNull();
        assertThat(resultEmpty).isEmpty();

        // Step 5: Verify
        verify(packageRepository, never()).findAllByIdWithStore(any());
        verify(packageExternalMapper, never()).toPackageDtoList(any());
    }

    // ================================================================
    // TEST: GET PACKAGE DTO - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should return PackageDTO for single ID")
    void shouldReturnPackageDTOForSingleId() {
        // Step 1: Arrange
        Long id = 1L;

        when(packageRepository.findByIdWithStore(id)).thenReturn(Optional.of(pkg));
        when(packageExternalMapper.toPackageDto(pkg)).thenReturn(packageDTO);

        // Step 2: Act
        PackageDTO result = packageExternalService.getPackageDto(id);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.totalWeightKg()).isEqualTo(10.0);
        assertThat(result.origin()).isNotNull();
        assertThat(result.origin().city()).isEqualTo("Buenos Aires");

        // Step 4: Verify
        verify(packageRepository).findByIdWithStore(id);
        verify(packageExternalMapper).toPackageDto(pkg);
    }

    // ================================================================
    // TEST: GET PACKAGE DTO - NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package not found")
    void shouldThrowExceptionWhenPackageNotFound() {
        // Step 1: Arrange
        Long id = 999L;

        when(packageRepository.findByIdWithStore(id)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageExternalService.getPackageDto(id))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Package not found: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(packageRepository).findByIdWithStore(id);
        verify(packageExternalMapper, never()).toPackageDto(any());
    }

    // ================================================================
    // TEST: GET PACKAGE DTO - INVALID ID
    // ================================================================

    @Test
    @DisplayName("Should throw exception when ID is invalid")
    void shouldThrowExceptionWhenIdIsInvalid() {
        // Step 1: Act & Assert with null
        assertThatThrownBy(() -> packageExternalService.getPackageDto(null))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Invalid package ID: null")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 2: Act & Assert with zero
        assertThatThrownBy(() -> packageExternalService.getPackageDto(0L))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Invalid package ID: 0")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 3: Verify
        verify(packageRepository, never()).findByIdWithStore(any());
        verify(packageExternalMapper, never()).toPackageDto(any());
    }
}