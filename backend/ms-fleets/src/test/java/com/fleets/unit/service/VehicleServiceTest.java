package com.fleets.unit.service;
/* 
import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleDetailDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.VehicleRepository;
import com.fleets.service.CategoryService;
import com.fleets.service.impl.VehicleServiceImpl;
import com.fleets.mapper.VehicleMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


// import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Unit Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Vehicle vehicle;
    private VehicleRequestDTO requestDTO;
    private Category category;
    private VehicleDetailDTO detailDTO;

    @BeforeEach
    void setUp() {
        // Setup test data
        category = new Category();
        category.setId(1L);
        category.setName("Sedan");

        vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setLicensePlate("ABC123");
        vehicle.setYear(2023);
        vehicle.setCategory(category);

        requestDTO = new VehicleRequestDTO();
        requestDTO.setLicensePlate("ABC123");
        requestDTO.setYear(2023);
        requestDTO.setCategoryId(1L);

        detailDTO = new VehicleDetailDTO(1L, "ABC123", 2023, null);
    }

    @Nested
    @DisplayName("Get Vehicle Tests")
    class GetVehicleTests {

        @Test
        @DisplayName("Should return vehicle when ID exists")
        void shouldReturnVehicleWhenIdExists() {
            // Given
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));

            // When
            Vehicle result = vehicleService.getVehicleEntityById(1L);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getLicensePlate()).isEqualTo("ABC123");
            verify(vehicleRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw AppException when vehicle not found")
        void shouldThrowExceptionWhenVehicleNotFound() {
            // Given
            when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> vehicleService.getVehicleEntityById(999L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Vehicle not found with id: 999");
            verify(vehicleRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("Create Vehicle Tests")
    class CreateVehicleTests {

        @Test
        @DisplayName("Should create vehicle successfully")
        void shouldCreateVehicleSuccessfully() {
            // Given
            when(vehicleRepository.existsByLicensePlate("ABC123")).thenReturn(false);
            when(categoryService.getCategoryEntityById(1L)).thenReturn(category);
            when(vehicleMapper.toEntity(any(VehicleRequestDTO.class))).thenReturn(vehicle);
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(vehicle);
            when(vehicleRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(vehicle));
            when(vehicleMapper.toDetailDto(any(Vehicle.class))).thenReturn(detailDTO);

            // When
            VehicleDetailDTO result = vehicleService.createVehicle(requestDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.licensePlate()).isEqualTo("ABC123");
            verify(vehicleRepository).save(any(Vehicle.class));
            verify(vehicleRepository).existsByLicensePlate("ABC123");
            verify(categoryService).getCategoryEntityById(1L);
        }

        @Test
        @DisplayName("Should throw exception when license plate already exists")
        void shouldThrowExceptionWhenDuplicateLicensePlate() {
            // Given
            when(vehicleRepository.existsByLicensePlate("ABC123")).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> vehicleService.createVehicle(requestDTO))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("License plate already exists");
            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("Update Vehicle Tests")
    class UpdateVehicleTests {

        @Test
        @DisplayName("Should update vehicle successfully")
        void shouldUpdateVehicleSuccessfully() {
            // Given
            Vehicle existingVehicle = new Vehicle();
            existingVehicle.setId(1L);
            existingVehicle.setLicensePlate("ABC123");
            existingVehicle.setYear(2022);

            VehicleRequestDTO updateDTO = new VehicleRequestDTO();
            updateDTO.setLicensePlate("XYZ789");
            updateDTO.setYear(2024);
            updateDTO.setCategoryId(1L);

            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(existingVehicle));
            when(vehicleRepository.existsByLicensePlate("XYZ789")).thenReturn(false);
            when(categoryService.getCategoryEntityById(1L)).thenReturn(category);
            when(vehicleRepository.save(any(Vehicle.class))).thenReturn(existingVehicle);
            when(vehicleRepository.findByIdWithCategory(1L)).thenReturn(Optional.of(existingVehicle));
            when(vehicleMapper.toDetailDto(any(Vehicle.class))).thenReturn(
                new VehicleDetailDTO(1L, "XYZ789", 2024, null)
            );

            // When
            VehicleDetailDTO result = vehicleService.updateVehicle(1L, updateDTO);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.licensePlate()).isEqualTo("XYZ789");
            assertThat(result.year()).isEqualTo(2024);
            verify(vehicleRepository).save(existingVehicle);
        }
    }

    @Nested
    @DisplayName("Delete Vehicle Tests")
    class DeleteVehicleTests {

        @Test
        @DisplayName("Should delete vehicle successfully")
        void shouldDeleteVehicleSuccessfully() {
            // Given
            when(vehicleRepository.findById(1L)).thenReturn(Optional.of(vehicle));
            doNothing().when(vehicleRepository).deleteById(1L);

            // When
            vehicleService.deleteVehicle(1L);

            // Then
            verify(vehicleRepository).deleteById(1L);
        }
    }
} */