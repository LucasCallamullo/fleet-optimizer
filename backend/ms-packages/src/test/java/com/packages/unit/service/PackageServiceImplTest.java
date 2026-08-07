package com.packages.unit.service;

import com.packages.dto.request.PackageRequestDTO;
import com.packages.dto.response.PackageDetailDTO;
import com.packages.dto.response.PackageResponseDTO;
import com.packages.dto.response.StoreResponseDTO;
import com.packages.exception.AppException;
import com.packages.mapper.PackageMapper;
import com.packages.model.embedded.Location;
import com.packages.model.entity.Package;
import com.packages.model.entity.Store;
import com.packages.model.enums.PackageStatus;
import com.packages.repository.PackageRepository;
import com.packages.repository.StoreRepository;
import com.packages.service.impl.PackageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Package Service Unit Tests")
class PackageServiceImplTest {

    @Mock
    private PackageRepository packageRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private PackageMapper packageMapper;

    @InjectMocks
    private PackageServiceImpl packageService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private Package pkg;
    private Package savedPkg;
    private Store store;
    private Location location;
    private PackageRequestDTO request;
    private PackageResponseDTO responseDTO;
    private PackageDetailDTO detailDTO;
    private StoreResponseDTO storeDTO;

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
        store.setOwnerId("admin-id");

        // Step 3: Create package
        pkg = new Package();
        pkg.setId(1L);
        pkg.setTrackingNumber("PKG-001");
        pkg.setTotalWeightKg(10.0);
        pkg.setTotalVolumeCbm(0.30);
        pkg.setStore(store);
        pkg.setOwnerId("user-id");
        pkg.setStatus(PackageStatus.CREATED);
        pkg.setCreatedAt(LocalDateTime.now());
        pkg.setUpdatedAt(LocalDateTime.now());

        // Step 4: Create saved package
        savedPkg = new Package();
        savedPkg.setId(1L);
        savedPkg.setTrackingNumber("PKG-001");
        savedPkg.setTotalWeightKg(10.0);
        savedPkg.setTotalVolumeCbm(0.30);
        savedPkg.setStore(store);
        savedPkg.setOwnerId("user-id");
        savedPkg.setStatus(PackageStatus.CREATED);
        savedPkg.setCreatedAt(LocalDateTime.now());
        savedPkg.setUpdatedAt(LocalDateTime.now());

        // Step 5: Create request DTO
        request = new PackageRequestDTO(
            "PKG-001",
            10.0,
            0.30,
            1L
        );

        // Step 6: Create store response DTO
        storeDTO = new StoreResponseDTO(
            1L,
            "Downtown Warehouse",
            "Main warehouse",
            location,
            "admin-id"
        );

        // Step 7: Create response DTOs
        responseDTO = new PackageResponseDTO(
            1L,
            "PKG-001",
            10.0,
            0.30,
            PackageStatus.CREATED,
            1L,
            "user-id"
        );

        detailDTO = new PackageDetailDTO(
            1L,
            "PKG-001",
            10.0,
            0.30,
            PackageStatus.CREATED,
            "user-id",
            LocalDateTime.now(),
            LocalDateTime.now(),
            storeDTO
        );
    }

    // ================================================================
    // TEST: CREATE PACKAGE - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should create package successfully")
    void shouldCreatePackage() {
        // Step 1: Arrange
        String ownerId = "user-id";

        when(packageRepository.findByTrackingNumber(request.trackingNumber()))
            .thenReturn(Optional.empty());
        when(storeRepository.findById(request.storeId()))
            .thenReturn(Optional.of(store));
        when(packageMapper.toEntity(request)).thenReturn(pkg);
        when(packageRepository.save(any(Package.class))).thenReturn(savedPkg);
        when(packageRepository.findByIdWithStore(savedPkg.getId()))
            .thenReturn(Optional.of(savedPkg));
        when(packageMapper.toDetailDto(any(Package.class))).thenReturn(detailDTO);

        // Step 2: Act
        PackageDetailDTO result = packageService.createPackage(request, ownerId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.trackingNumber()).isEqualTo("PKG-001");
        assertThat(result.status()).isEqualTo(PackageStatus.CREATED);

        // Step 4: Verify
        verify(packageRepository).findByTrackingNumber(request.trackingNumber());
        verify(storeRepository).findById(request.storeId());
        verify(packageRepository).save(any(Package.class));
        verify(packageRepository).findByIdWithStore(savedPkg.getId());
        verify(packageMapper).toDetailDto(any(Package.class));
    }

    // ================================================================
    // TEST: CREATE PACKAGE - DUPLICATE TRACKING NUMBER
    // ================================================================

    @Test
    @DisplayName("Should throw exception when tracking number already exists")
    void shouldThrowExceptionWhenTrackingNumberExists() {
        // Step 1: Arrange
        String ownerId = "user-id";

        when(packageRepository.findByTrackingNumber(request.trackingNumber()))
            .thenReturn(Optional.of(pkg));

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.createPackage(request, ownerId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Tracking number already exists: PKG-001")
            .hasFieldOrPropertyWithValue("statusCode", 409);

        // Step 3: Verify
        verify(packageRepository).findByTrackingNumber(request.trackingNumber());
        verify(storeRepository, never()).findById(anyLong());
        verify(packageRepository, never()).save(any(Package.class));
    }

    // ================================================================
    // TEST: CREATE PACKAGE - STORE NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when store not found")
    void shouldThrowExceptionWhenStoreNotFound() {
        // Step 1: Arrange
        String ownerId = "user-id";

        when(packageRepository.findByTrackingNumber(request.trackingNumber()))
            .thenReturn(Optional.empty());
        when(storeRepository.findById(request.storeId()))
            .thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.createPackage(request, ownerId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Store not found: 1")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(packageRepository).findByTrackingNumber(request.trackingNumber());
        verify(storeRepository).findById(request.storeId());
        verify(packageRepository, never()).save(any(Package.class));
    }

    // ================================================================
    // TEST: GET ALL PACKAGES - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should return all packages as response DTOs")
    void shouldReturnAllPackages() {
        // Step 1: Arrange
        List<Package> packages = List.of(pkg);

        when(packageRepository.findAll()).thenReturn(packages);
        when(packageMapper.toResponseDtoList(packages)).thenReturn(List.of(responseDTO));

        // Step 2: Act
        List<PackageResponseDTO> result = packageService.getAllPackages();

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).trackingNumber()).isEqualTo("PKG-001");

        // Step 4: Verify
        verify(packageRepository).findAll();
        verify(packageMapper).toResponseDtoList(packages);
    }

    // ================================================================
    // TEST: GET ALL PACKAGES WITH STORE - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should return all packages with store details")
    void shouldReturnAllPackagesWithStore() {
        // Step 1: Arrange
        List<Package> packages = List.of(pkg);

        when(packageRepository.findAllWithStore()).thenReturn(packages);
        when(packageMapper.toDetailDtoList(packages)).thenReturn(List.of(detailDTO));

        // Step 2: Act
        List<PackageDetailDTO> result = packageService.getAllPackagesWithStore();

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);
        assertThat(result.get(0).store()).isNotNull();
        assertThat(result.get(0).store().id()).isEqualTo(1L);

        // Step 4: Verify
        verify(packageRepository).findAllWithStore();
        verify(packageMapper).toDetailDtoList(packages);
    }

    // ================================================================
    // TEST: GET PACKAGE BY ID - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should return package by ID")
    void shouldReturnPackageById() {
        // Step 1: Arrange
        Long packageId = 1L;

        when(packageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
        when(packageMapper.toResponseDto(pkg)).thenReturn(responseDTO);

        // Step 2: Act
        PackageResponseDTO result = packageService.getPackage(packageId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.trackingNumber()).isEqualTo("PKG-001");

        // Step 4: Verify
        verify(packageRepository).findById(packageId);
        verify(packageMapper).toResponseDto(pkg);
    }

    // ================================================================
    // TEST: GET PACKAGE BY ID - NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package not found")
    void shouldThrowExceptionWhenPackageNotFound() {
        // Step 1: Arrange
        Long packageId = 999L;

        when(packageRepository.findById(packageId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.getPackage(packageId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Package not found: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(packageRepository).findById(packageId);
        verify(packageMapper, never()).toResponseDto(any(Package.class));
    }

    // ================================================================
    // TEST: GET PACKAGE DETAIL - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should return package detail with store")
    void shouldReturnPackageDetail() {
        // Step 1: Arrange
        Long packageId = 1L;

        when(packageRepository.findByIdWithStore(packageId)).thenReturn(Optional.of(pkg));
        when(packageMapper.toDetailDto(pkg)).thenReturn(detailDTO);

        // Step 2: Act
        PackageDetailDTO result = packageService.getPackageDetail(packageId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.store()).isNotNull();
        assertThat(result.store().id()).isEqualTo(1L);

        // Step 4: Verify
        verify(packageRepository).findByIdWithStore(packageId);
        verify(packageMapper).toDetailDto(pkg);
    }

    // ================================================================
    // TEST: UPDATE PACKAGE - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should update package successfully")
    void shouldUpdatePackage() {
        // Step 1: Arrange
        Long packageId = 1L;
        PackageRequestDTO updateRequest = new PackageRequestDTO(
            "PKG-002",
            15.0,
            0.50,
            1L
        );

        Package updatedPkg = new Package();
        updatedPkg.setId(1L);
        updatedPkg.setTrackingNumber("PKG-002");
        updatedPkg.setTotalWeightKg(15.0);
        updatedPkg.setTotalVolumeCbm(0.50);
        updatedPkg.setStore(store);
        updatedPkg.setOwnerId("user-id");
        updatedPkg.setStatus(PackageStatus.CREATED);

        when(packageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
        when(packageRepository.findByTrackingNumber(updateRequest.trackingNumber()))
            .thenReturn(Optional.empty());
        when(packageRepository.save(any(Package.class))).thenReturn(updatedPkg);
        when(packageRepository.findByIdWithStore(packageId)).thenReturn(Optional.of(updatedPkg));
        when(packageMapper.toDetailDto(any(Package.class))).thenReturn(
            new PackageDetailDTO(
                1L,
                "PKG-002",
                15.0,
                0.50,
                PackageStatus.CREATED,
                "user-id",
                LocalDateTime.now(),
                LocalDateTime.now(),
                storeDTO
            )
        );

        // Step 2: Act
        PackageDetailDTO result = packageService.updatePackage(packageId, updateRequest);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.trackingNumber()).isEqualTo("PKG-002");
        assertThat(result.totalWeightKg()).isEqualTo(15.0);
        assertThat(result.totalVolumeCbm()).isEqualTo(0.50);

        // Step 4: Verify
        verify(packageRepository).findById(packageId);
        verify(packageRepository).findByTrackingNumber(updateRequest.trackingNumber());
        verify(packageRepository).save(any(Package.class));
    }

    // ================================================================
    // TEST: UPDATE PACKAGE - DUPLICATE TRACKING NUMBER
    // ================================================================

    @Test
    @DisplayName("Should throw exception when updating to existing tracking number")
    void shouldThrowExceptionWhenUpdatingToExistingTrackingNumber() {
        // Step 1: Arrange
        Long packageId = 1L;
        Package existingPkgWithSameTracking = new Package();
        existingPkgWithSameTracking.setId(2L);
        existingPkgWithSameTracking.setTrackingNumber("PKG-002");

        PackageRequestDTO updateRequest = new PackageRequestDTO(
            "PKG-002",
            15.0,
            0.50,
            1L
        );

        when(packageRepository.findById(packageId)).thenReturn(Optional.of(pkg));
        when(packageRepository.findByTrackingNumber(updateRequest.trackingNumber()))
            .thenReturn(Optional.of(existingPkgWithSameTracking));

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.updatePackage(packageId, updateRequest))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Tracking number already exists: PKG-002")
            .hasFieldOrPropertyWithValue("statusCode", 409);

        // Step 3: Verify
        verify(packageRepository).findById(packageId);
        verify(packageRepository).findByTrackingNumber(updateRequest.trackingNumber());
        verify(packageRepository, never()).save(any(Package.class));
    }

    // ================================================================
    // TEST: DELETE PACKAGE - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should delete package successfully")
    void shouldDeletePackage() {
        // Step 1: Arrange
        Long packageId = 1L;

        when(packageRepository.findById(packageId)).thenReturn(Optional.of(pkg));

        // Step 2: Act
        packageService.deletePackage(packageId);

        // Step 3: Verify
        verify(packageRepository).findById(packageId);
        verify(packageRepository).delete(pkg);
    }

    // ================================================================
    // TEST: DELETE PACKAGE - NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when deleting non-existent package")
    void shouldThrowExceptionWhenDeletingNonExistentPackage() {
        // Step 1: Arrange
        Long packageId = 999L;

        when(packageRepository.findById(packageId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.deletePackage(packageId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Package not found: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(packageRepository).findById(packageId);
        verify(packageRepository, never()).delete(any(Package.class));
    }

    // ================================================================
    // TEST: UPDATE PACKAGE STATUS - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should update package status to IN_TRANSIT when READY_FOR_PICKUP")
    void shouldUpdateStatusToInTransit() {
        // Step 1: Arrange - Package with READY_FOR_PICKUP status
        Package readyPackage = new Package();
        readyPackage.setId(1L);
        readyPackage.setTrackingNumber("PKG-001");
        readyPackage.setStatus(PackageStatus.READY_FOR_PICKUP);

        when(packageRepository.findAllById(List.of(1L)))
            .thenReturn(List.of(readyPackage));

        // Step 2: Act
        packageService.updatePackageStatus(List.of(1L), "IN_TRANSIT");

        // Step 3: Assert
        assertThat(readyPackage.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);

        // Step 4: Verify
        verify(packageRepository).findAllById(List.of(1L));
        verify(packageRepository).saveAll(List.of(readyPackage));
    }

    @Test
    @DisplayName("Should update multiple packages to IN_TRANSIT")
    void shouldUpdateMultiplePackagesStatus() {
        // Step 1: Arrange - Two packages with READY_FOR_PICKUP
        Package pkg1 = new Package();
        pkg1.setId(1L);
        pkg1.setTrackingNumber("PKG-001");
        pkg1.setStatus(PackageStatus.READY_FOR_PICKUP);

        Package pkg2 = new Package();
        pkg2.setId(2L);
        pkg2.setTrackingNumber("PKG-002");
        pkg2.setStatus(PackageStatus.READY_FOR_PICKUP);

        when(packageRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(pkg1, pkg2));

        // Step 2: Act
        packageService.updatePackageStatus(List.of(1L, 2L), "IN_TRANSIT");

        // Step 3: Assert
        assertThat(pkg1.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);
        assertThat(pkg2.getStatus()).isEqualTo(PackageStatus.IN_TRANSIT);

        // Step 4: Verify
        verify(packageRepository).findAllById(List.of(1L, 2L));
        verify(packageRepository).saveAll(List.of(pkg1, pkg2));
    }

    // ================================================================
    // TEST: UPDATE PACKAGE STATUS - INVALID STATUS
    // ================================================================

    @Test
    @DisplayName("Should throw exception when status is invalid")
    void shouldThrowExceptionWhenStatusInvalid() {
        // Step 1: Arrange - NO mock necesario porque el método nunca llega al repository

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.updatePackageStatus(List.of(1L), "INVALID_STATUS"))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Invalid status: INVALID_STATUS")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 3: Verify - El repository NUNCA se llama
        verify(packageRepository, never()).findAllById(any());
        verify(packageRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: UPDATE PACKAGE STATUS - PACKAGE NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package not found")
    void shouldThrowExceptionWhenPackageNotFoundPack() {
        // Step 1: Arrange
        when(packageRepository.findAllById(List.of(999L)))
            .thenReturn(List.of());  // Empty list means not found

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.updatePackageStatus(List.of(999L), "IN_TRANSIT"))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Packages not found: [999]")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(packageRepository).findAllById(List.of(999L));
        verify(packageRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: UPDATE PACKAGE STATUS - INVALID TRANSITION
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package is not READY_FOR_PICKUP for IN_TRANSIT")
    void shouldThrowExceptionWhenPackageNotReadyForPickup() {
        // Step 1: Arrange - Package with CREATED status (not READY_FOR_PICKUP)
        Package createdPackage = new Package();
        createdPackage.setId(1L);
        createdPackage.setTrackingNumber("PKG-001");
        createdPackage.setStatus(PackageStatus.CREATED);

        when(packageRepository.findAllById(List.of(1L)))
            .thenReturn(List.of(createdPackage));

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.updatePackageStatus(List.of(1L), "IN_TRANSIT"))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("must be 'READY_FOR_PICKUP' to transition to IN_TRANSIT")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 3: Verify
        verify(packageRepository).findAllById(List.of(1L));
        verify(packageRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: UPDATE PACKAGE STATUS - MIXED PACKAGES (one invalid)
    // ================================================================

    @Test
    @DisplayName("Should throw exception when one package is not READY_FOR_PICKUP")
    void shouldThrowExceptionWhenOnePackageNotReady() {
        // Step 1: Arrange - One READY_FOR_PICKUP, one CREATED
        Package readyPackage = new Package();
        readyPackage.setId(1L);
        readyPackage.setStatus(PackageStatus.READY_FOR_PICKUP);

        Package createdPackage = new Package();
        createdPackage.setId(2L);
        createdPackage.setStatus(PackageStatus.CREATED);

        when(packageRepository.findAllById(List.of(1L, 2L)))
            .thenReturn(List.of(readyPackage, createdPackage));

        // Step 2: Act & Assert
        assertThatThrownBy(() -> packageService.updatePackageStatus(List.of(1L, 2L), "IN_TRANSIT"))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("must be 'READY_FOR_PICKUP'")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 3: Verify
        verify(packageRepository).findAllById(List.of(1L, 2L));
        verify(packageRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: UPDATE PACKAGE STATUS - NULL STATUS
    // ================================================================

    @Test
    @DisplayName("Should throw exception when status is null")
    void shouldThrowExceptionWhenStatusIsNull() {
        // Step 1: Act & Assert - validation fails before repository
        assertThatThrownBy(() -> packageService.updatePackageStatus(List.of(1L), null))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Invalid status: null")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 2: Verify - Repository never called
        verify(packageRepository, never()).findAllById(any());
        verify(packageRepository, never()).saveAll(any());
    }
}