package com.routes.unit.service;

import com.routes.client.FleetClient;
import com.routes.client.PackageClient;
import com.routes.dto.external.FleetVehicleDTO;
import com.routes.dto.external.PackageDTO;
import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.exception.AppException;
import com.routes.service.RouteValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Route Validation Service Unit Tests")
class RouteValidationServiceTest {

    @Mock
    private FleetClient fleetClient;

    @Mock
    private PackageClient packageClient;

    @InjectMocks
    private RouteValidationService validationService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private LegRequestDTO validLeg;
    private LegRequestDTO legWithHeavyPackage;
    private LegRequestDTO legWithLargePackage;
    private LegRequestDTO legWithMissingVehicle;
    private LegRequestDTO legWithMissingPackage;
    private List<LegRequestDTO> legs;
    private FleetVehicleDTO vehicle1;
    private FleetVehicleDTO vehicle2;
    private PackageDTO package1;
    private PackageDTO package2;
    private PackageDTO heavyPackage;
    private PackageDTO largePackage;

    @BeforeEach
    void setUp() {
        // Step 1: Create location DTOs
        LocationRequestDTO origin = new LocationRequestDTO(
            "Av. Libertador", "1000", "Buenos Aires", "CABA",
            "Argentina", "1000", -34.6037, -58.3816
        );

        LocationRequestDTO destination = new LocationRequestDTO(
            "Av. Colon", "500", "Cordoba", "Cordoba",
            "Argentina", "5000", -31.4201, -64.1888
        );

        // Step 2: Create vehicles
        vehicle1 = new FleetVehicleDTO(1L, "ABC123", 100.0, 50.0, "AVAILABLE");
        vehicle2 = new FleetVehicleDTO(2L, "DEF456", 200.0, 100.0, "AVAILABLE");

        // Step 3: Create packages
        package1 = new PackageDTO(1L, "PKG-001", 10.0, 5.0);
        package2 = new PackageDTO(2L, "PKG-002", 20.0, 10.0);
        heavyPackage = new PackageDTO(3L, "PKG-003", 150.0, 10.0);  // > 100kg (vehicle1 capacity)
        largePackage = new PackageDTO(4L, "PKG-004", 10.0, 60.0);   // > 50m³ (vehicle1 capacity)

        // Step 4: Create valid leg
        validLeg = new LegRequestDTO(1, 1L, 1L, origin, destination);

        // Step 5: Create leg with heavy package (exceeds weight)
        legWithHeavyPackage = new LegRequestDTO(2, 1L, 3L, origin, destination);

        // Step 6: Create leg with large package (exceeds volume)
        legWithLargePackage = new LegRequestDTO(3, 1L, 4L, origin, destination);

        // Step 7: Create leg with missing vehicle
        legWithMissingVehicle = new LegRequestDTO(4, 999L, 1L, origin, destination);

        // Step 8: Create leg with missing package
        legWithMissingPackage = new LegRequestDTO(5, 1L, 999L, origin, destination);

        legs = List.of(validLeg);
    }

    // ================================================================
    // TEST: VALID LEG - SUCCESS
    // ================================================================

    @Test
    @DisplayName("Should validate legs successfully when all data is valid")
    void shouldValidateLegsSuccessfully() {
        // Step 1: Arrange - Mock external clients
        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicle1));
        
        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(package1));

        // Step 2: Act & Assert - Should not throw any exception
        assertThatCode(() -> validationService.validateLegs(legs))
            .doesNotThrowAnyException();

        // Step 3: Verify
        verify(fleetClient).getVehiclesByIds(List.of(1L));
        verify(packageClient).getPackagesByIds(List.of(1L));
    }

    // ================================================================
    // TEST: VEHICLE NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when vehicle is not found")
    void shouldThrowExceptionWhenVehicleNotFound() {
        // Step 1: Arrange - Mock returns vehicles that don't include the requested one
        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of());  // Empty list - vehicle not found

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(package1));

        List<LegRequestDTO> legsWithInvalidVehicle = List.of(legWithMissingVehicle);

        // Step 2: Act & Assert
        assertThatThrownBy(() -> validationService.validateLegs(legsWithInvalidVehicle))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Vehicle not found: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);
    }

    // ================================================================
    // TEST: PACKAGE NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package is not found")
    void shouldThrowExceptionWhenPackageNotFound() {
        // Step 1: Arrange - Mock returns packages that don't include the requested one
        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicle1));

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of());  // Empty list - package not found

        List<LegRequestDTO> legsWithInvalidPackage = List.of(legWithMissingPackage);

        // Step 2: Act & Assert
        assertThatThrownBy(() -> validationService.validateLegs(legsWithInvalidPackage))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Package not found: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);
    }

    // ================================================================
    // TEST: WEIGHT CAPACITY EXCEEDED
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package weight exceeds vehicle capacity")
    void shouldThrowExceptionWhenWeightExceedsCapacity() {
        // Step 1: Arrange - Mock external clients
        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicle1));  // maxWeight = 100kg

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(heavyPackage));  // totalWeight = 150kg

        List<LegRequestDTO> legsWithHeavyPackage = List.of(legWithHeavyPackage);

        // Step 2: Act & Assert
        assertThatThrownBy(() -> validationService.validateLegs(legsWithHeavyPackage))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("statusCode", 400);
    }

    // ================================================================
    // TEST: VOLUME CAPACITY EXCEEDED
    // ================================================================

    @Test
    @DisplayName("Should throw exception when package volume exceeds vehicle capacity")
    void shouldThrowExceptionWhenVolumeExceedsCapacity() {
        // Step 1: Arrange - Mock external clients
        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicle1));  // maxVolume = 50m³

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(largePackage));  // totalVolume = 60m³

        List<LegRequestDTO> legsWithLargePackage = List.of(legWithLargePackage);

        // Step 2: Act & Assert
        assertThatThrownBy(() -> validationService.validateLegs(legsWithLargePackage))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("statusCode", 400);
    }

    // ================================================================
    // TEST: MULTIPLE LEGS VALIDATION
    // ================================================================

    @Test
    @DisplayName("Should validate multiple legs successfully")
    void shouldValidateMultipleLegs() {
        // Step 1: Arrange
        LegRequestDTO leg1 = new LegRequestDTO(
            1, 1L, 1L, 
            new LocationRequestDTO("Street 1", "100", "City1", "State", "Country", "1000", 0.0, 0.0),
            new LocationRequestDTO("Street 2", "200", "City2", "State", "Country", "2000", 0.0, 0.0)
        );

        LegRequestDTO leg2 = new LegRequestDTO(
            2, 2L, 2L,
            new LocationRequestDTO("Street 3", "300", "City3", "State", "Country", "3000", 0.0, 0.0),
            new LocationRequestDTO("Street 4", "400", "City4", "State", "Country", "4000", 0.0, 0.0)
        );

        List<LegRequestDTO> multipleLegs = List.of(leg1, leg2);

        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicle1, vehicle2));

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(package1, package2));

        // Step 2: Act & Assert
        assertThatCode(() -> validationService.validateLegs(multipleLegs))
            .doesNotThrowAnyException();

        // Step 3: Verify
        verify(fleetClient).getVehiclesByIds(List.of(1L, 2L));
        verify(packageClient).getPackagesByIds(List.of(1L, 2L));
    }

    // ================================================================
    // TEST: MULTIPLE LEGS WITH ONE FAILING
    // ================================================================

    @Test
    @DisplayName("Should throw exception when one leg fails validation")
    void shouldThrowExceptionWhenOneLegFails() {
        // Step 1: Arrange
        LegRequestDTO validLeg2 = new LegRequestDTO(
            1, 2L, 2L,
            new LocationRequestDTO("Street 1", "100", "City1", "State", "Country", "1000", 0.0, 0.0),
            new LocationRequestDTO("Street 2", "200", "City2", "State", "Country", "2000", 0.0, 0.0)
        );

        List<LegRequestDTO> mixedLegs = List.of(validLeg2, legWithHeavyPackage);

        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicle1, vehicle2));  // vehicle1 maxWeight=100

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(package2, heavyPackage));  // heavyPackage=150kg

        // Step 2: Act & Assert
        assertThatThrownBy(() -> validationService.validateLegs(mixedLegs))
            .isInstanceOf(AppException.class);
    }

    // ================================================================
    // TEST: FEIGN CLIENT ERROR HANDLING
    // ================================================================

    @Test
    @DisplayName("Should propagate Feign client exceptions")
    void shouldPropagateFeignClientExceptions() {
        // Step 1: Arrange - Mock Feign client throws exception
        when(fleetClient.getVehiclesByIds(anyList()))
            .thenThrow(new RuntimeException("Fleet service unavailable"));

        // Step 2: Act & Assert
        assertThatThrownBy(() -> validationService.validateLegs(legs))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Fleet service unavailable");
    }

    // ================================================================
    // TEST: NULL CAPACITY HANDLING
    // ================================================================

    @Test
    @DisplayName("Should not validate capacity when values are null")
    void shouldNotValidateCapacityWhenValuesAreNull() {
        // Step 1: Arrange - Vehicle with null capacities
        FleetVehicleDTO vehicleWithNullCapacity = new FleetVehicleDTO(
            1L, "ABC123", null, null, "AVAILABLE"
        );

        // Package with null values
        PackageDTO packageWithNullValues = new PackageDTO(
            1L, "PKG-001", null, null
        );

        LegRequestDTO legWithNullValues = new LegRequestDTO(
            1, 1L, 1L,
            new LocationRequestDTO("Street", "100", "City", "State", "Country", "1000", 0.0, 0.0),
            new LocationRequestDTO("Street2", "200", "City2", "State", "Country", "2000", 0.0, 0.0)
        );

        when(fleetClient.getVehiclesByIds(anyList()))
            .thenReturn(List.of(vehicleWithNullCapacity));

        when(packageClient.getPackagesByIds(anyList()))
            .thenReturn(List.of(packageWithNullValues));

        // Step 2: Act & Assert - Should not throw validation exceptions
        assertThatCode(() -> validationService.validateLegs(List.of(legWithNullValues)))
            .doesNotThrowAnyException();
    }
}