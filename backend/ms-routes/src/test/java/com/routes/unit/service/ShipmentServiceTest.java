package com.routes.unit.service;

import com.routes.client.FleetClient;
import com.routes.client.GeocodingClient;
import com.routes.client.PackageClient;
import com.routes.dto.client.common.LocationDTO;
import com.routes.dto.client.fleets.FleetVehicleDTO;
import com.routes.dto.client.geocoding.BatchDistanceRequest;
import com.routes.dto.client.geocoding.BatchDistanceResponse;
import com.routes.dto.client.geocoding.DistanceResult;
import com.routes.dto.client.packages.PackageDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.dto.request.ShipmentRequestDTO;
import com.routes.dto.response.ShipmentResponseDTO;
import com.routes.exception.AppException;
import com.routes.mapper.LocationMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Location;
import com.routes.model.entity.Route;
import com.routes.model.enums.LegStatus;
import com.routes.model.enums.RouteStatus;
import com.routes.service.LegService;
import com.routes.service.RouteService;
import com.routes.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Shipment Service Unit Tests")
class ShipmentServiceTest {

    @Mock
    private RouteService routeService;

    @Mock
    private LocationMapper locationMapper;

    @Mock
    private LegService legService;

    @Mock
    private PackageClient packageClient;

    @Mock
    private FleetClient fleetClient;

    @Mock
    private GeocodingClient geocodingClient;

    @InjectMocks
    private ShipmentService shipmentService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private ShipmentRequestDTO request;
    private List<PackageDTO> packages;
    private FleetVehicleDTO vehicle;
    private Route route;
    private Route savedRoute;
    private List<Leg> legs;
    private Location origin;
    private Location destination;
    private BatchDistanceResponse geocodingResponse;

    @BeforeEach
    void setUp() {
        // Step 1: Create origin location
        origin = new Location();
        origin.setLatitude(-34.6037);
        origin.setLongitude(-58.3816);
        origin.setCity("Buenos Aires");

        // Step 2: Create destination location
        destination = new Location();
        destination.setLatitude(-31.4201);
        destination.setLongitude(-64.1888);
        destination.setCity("Cordoba");

        // Step 3: Create LocationRequestDTO for destination
        LocationRequestDTO destDTO = new LocationRequestDTO(
            "Av. Colon", "500", "Cordoba", "Cordoba",
            "Argentina", "5000", -31.4201, -64.1888
        );

        // Step 4: Create request
        request = new ShipmentRequestDTO(
            List.of(1L, 2L),
            10L,
            destDTO
        );

        // Step 4: Create LocationDTO for package origin
        LocationDTO originDTO = new LocationDTO(
            "Av. Libertador", "1000", "Buenos Aires", "CABA",
            "Argentina", "1000", -34.6037, -58.3816
        );

        // Step 5: Create packages
        PackageDTO pkg1 = new PackageDTO(1L, 10.0, 0.30, originDTO);
        PackageDTO pkg2 = new PackageDTO(2L, 20.0, 0.50, originDTO);
        packages = List.of(pkg1, pkg2);

        // Step 6: Create vehicle
        vehicle = new FleetVehicleDTO(
            10L, "ABC123", 10.5, 1.50, 2.50, 100.0, 50.0, "AVAILABLE"
        );

        // Step 7: Create route
        route = new Route();
        route.setName("Shipment-20260101-120000");
        route.setStatus(RouteStatus.PLANNED);

        // Step 8: Create saved route
        savedRoute = new Route();
        savedRoute.setId(1L);
        savedRoute.setName("Shipment-20260101-120000");
        savedRoute.setStatus(RouteStatus.PLANNED);
        savedRoute.setCreatedAt(LocalDateTime.now());

        // Step 9: Create legs
        Leg leg1 = new Leg();
        leg1.setId(1L);
        leg1.setSequence(1);
        leg1.setRoute(savedRoute);
        leg1.setVehicleId(10L);
        leg1.setPackageId(1L);
        leg1.setOrigin(origin);
        leg1.setDestination(destination);
        leg1.setStatus(LegStatus.PENDING);
        leg1.setDistanceKm(700.0);
        leg1.setDurationMinutes(480);

        Leg leg2 = new Leg();
        leg2.setId(2L);
        leg2.setSequence(2);
        leg2.setRoute(savedRoute);
        leg2.setVehicleId(10L);
        leg2.setPackageId(2L);
        leg2.setOrigin(origin);
        leg2.setDestination(destination);
        leg2.setStatus(LegStatus.PENDING);
        leg2.setDistanceKm(700.0);
        leg2.setDurationMinutes(480);

        legs = List.of(leg1, leg2);

        // Step 10: Create geocoding response
        geocodingResponse = new BatchDistanceResponse(List.of(
            new DistanceResult(0L, 700.0, 480),
            new DistanceResult(1L, 700.0, 480)
        ));
    }

    // ================================================================
    // TEST 1: SUCCESSFUL SHIPMENT CREATION
    // ================================================================

    @Test
    @DisplayName("Should create shipment successfully with valid data")
    void shouldCreateShipmentSuccessfully() {
        // Step 1: Mock external calls
        when(packageClient.getPackagesByIds(request.packageIds()))
            .thenReturn(packages);
        when(fleetClient.getVehicleById(request.vehicleId()))
            .thenReturn(vehicle);

        when(locationMapper.toEntity(request.destination()))
            .thenReturn(destination);
        when(locationMapper.toEntity(any(LocationDTO.class)))
            .thenReturn(origin);

        when(locationMapper.toDto(any()))
            .thenReturn(null); // Not needed for this test

        when(geocodingClient.calculateDistances(any(BatchDistanceRequest.class)))
            .thenReturn(geocodingResponse);

        // when(routeService.save(any(Route.class)))
        //    .thenReturn(savedRoute);
        when(routeService.save(any(Route.class)))
            .thenAnswer(invocation -> {
                Route routeArg = invocation.getArgument(0);
                routeArg.setId(1L);  // Simular que Hibernate asigna ID
                routeArg.setCreatedAt(LocalDateTime.now());  // ← Simular @CreationTimestamp
                routeArg.setUpdatedAt(LocalDateTime.now());  // ← Simular @UpdateTimestamp
                routeArg.setEstimatedDistanceKm(1400.0);
                routeArg.setEstimatedDurationMinutes(960);
                return routeArg;     // ← Devuelve el MISMO objeto
            });

        when(legService.saveAllLegs(anyList(), any(Route.class)))
            .thenReturn(legs);

        doNothing().when(packageClient).updatePackageStatus(any());    // void method uses doNothing

        // Step 2: Execute
        ShipmentResponseDTO result = shipmentService.createShipment(request);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.routeId()).isEqualTo(1L);
        assertThat(result.vehicleId()).isEqualTo(10L);
        assertThat(result.legs()).hasSize(2);
        assertThat(result.totalDistanceKm()).isEqualTo(1400.0);
        assertThat(result.totalDurationMinutes()).isEqualTo(960);
      
        // Step 4: Verify interactions
        verify(packageClient).getPackagesByIds(request.packageIds());
        verify(fleetClient).getVehicleById(request.vehicleId());
        verify(geocodingClient).calculateDistances(any(BatchDistanceRequest.class));
        verify(routeService).save(any(Route.class));
        verify(legService).saveAllLegs(anyList(), any(Route.class));
        verify(packageClient).updatePackageStatus(any());
    }
   
    // ================================================================
    // TEST 2: EMPTY PACKAGES LIST
    // ================================================================

    @Test
    @DisplayName("Should throw exception when no packages found")
    void shouldThrowExceptionWhenNoPackagesFound() {
        // Step 1: Mock external calls
        when(packageClient.getPackagesByIds(request.packageIds()))
            .thenReturn(List.of());

        // Step 2: Execute and assert
        assertThatThrownBy(() -> shipmentService.createShipment(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("No packages found")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify no other calls were made
        verify(fleetClient, never()).getVehicleById(anyLong());
        verify(routeService, never()).save(any(Route.class));
        verify(legService, never()).saveAllLegs(anyList(), any(Route.class));
    }

    // ================================================================
    // TEST 3: VEHICLE WEIGHT CAPACITY EXCEEDED
    // ================================================================

    @Test
    @DisplayName("Should throw exception when total weight exceeds vehicle capacity")
    void shouldThrowExceptionWhenWeightExceedsCapacity() {
        // Step 1: Create vehicle with low weight capacity
        FleetVehicleDTO lowCapacityVehicle = new FleetVehicleDTO(
            10L, "ABC123", 10.5, 1.50, 2.50, 15.0, 50.0, "AVAILABLE"
        );

        // Step 2: Mock external calls
        when(packageClient.getPackagesByIds(request.packageIds()))
            .thenReturn(packages);
        when(fleetClient.getVehicleById(request.vehicleId()))
            .thenReturn(lowCapacityVehicle);

        // Step 3: Execute and assert
        assertThatThrownBy(() -> shipmentService.createShipment(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Total weight")
            .hasMessageContaining("exceeds vehicle capacity")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 4: Verify route was not saved
        verify(routeService, never()).save(any(Route.class));
        verify(legService, never()).saveAllLegs(anyList(), any(Route.class));
    }

    // ================================================================
    // TEST 4: VEHICLE VOLUME CAPACITY EXCEEDED
    // ================================================================

    @Test
    @DisplayName("Should throw exception when total volume exceeds vehicle capacity")
    void shouldThrowExceptionWhenVolumeExceedsCapacity() {
        // Step 1: Create vehicle with low volume capacity
        FleetVehicleDTO lowCapacityVehicle = new FleetVehicleDTO(
            10L, "ABC123", 10.5, 1.50, 2.50, 100.0, 0.60, "AVAILABLE"
        );

        // Step 2: Mock external calls
        when(packageClient.getPackagesByIds(request.packageIds()))
            .thenReturn(packages);
        when(fleetClient.getVehicleById(request.vehicleId()))
            .thenReturn(lowCapacityVehicle);

        // Step 3: Execute and assert
        assertThatThrownBy(() -> shipmentService.createShipment(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Total volume")
            .hasMessageContaining("exceeds vehicle capacity")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 4: Verify route was not saved
        verify(routeService, never()).save(any(Route.class));
        verify(legService, never()).saveAllLegs(anyList(), any(Route.class));
    }

    // ================================================================
    // TEST 5: VEHICLE NOT FOUND
    // ================================================================

    @Test
    @DisplayName("Should throw exception when vehicle not found")
    void shouldThrowExceptionWhenVehicleNotFound() {
        // Step 1: Mock external calls
        when(packageClient.getPackagesByIds(request.packageIds()))
            .thenReturn(packages);
        when(fleetClient.getVehicleById(request.vehicleId()))
            .thenThrow(new AppException("Vehicle not found: 10", 404));

        // Step 2: Execute and assert
        assertThatThrownBy(() -> shipmentService.createShipment(request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Vehicle not found")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify route was not saved
        verify(routeService, never()).save(any(Route.class));
        verify(legService, never()).saveAllLegs(anyList(), any(Route.class));
    }

    // ================================================================
    // TEST 6: NULL CAPACITY HANDLING
    // ================================================================

    @Test
    @DisplayName("Should handle null capacity values gracefully")
    void shouldHandleNullCapacityValues() {
        // Step 1: Create vehicle with null capacities
        FleetVehicleDTO nullCapacityVehicle = new FleetVehicleDTO(
            10L, "ABC123", 10.5, 1.50, 2.50, null, null, "AVAILABLE"
        );

        LocationDTO originDTO = new LocationDTO(
            "Av. Libertador", "1000", "Buenos Aires", "CABA",
            "Argentina", "1000", -34.6037, -58.3816
        );

        // Step 2: Create packages with null capacities
        PackageDTO pkg1 = new PackageDTO(1L, null, null, originDTO);
        PackageDTO pkg2 = new PackageDTO(2L, null, null, originDTO);
        List<PackageDTO> packagesWithNull = List.of(pkg1, pkg2);

        // Step 3: Mock external calls
        when(packageClient.getPackagesByIds(request.packageIds()))
            .thenReturn(packagesWithNull);
        when(fleetClient.getVehicleById(request.vehicleId()))
            .thenReturn(nullCapacityVehicle);

        when(locationMapper.toEntity(request.destination()))
            .thenReturn(destination);
        
        when(locationMapper.toEntity(any(LocationDTO.class)))
             .thenReturn(origin);

        when(locationMapper.toDto(any()))
            .thenReturn(null); // Not needed for this test

        when(geocodingClient.calculateDistances(any(BatchDistanceRequest.class)))
            .thenReturn(geocodingResponse);

        when(routeService.save(any(Route.class)))
            .thenAnswer(invocation -> {
                Route routeArg = invocation.getArgument(0);
                routeArg.setId(1L);  // Simular que Hibernate asigna ID
                routeArg.setCreatedAt(LocalDateTime.now());  // ← Simular @CreationTimestamp
                routeArg.setUpdatedAt(LocalDateTime.now());  // ← Simular @UpdateTimestamp
                routeArg.setEstimatedDistanceKm(1400.0);
                routeArg.setEstimatedDurationMinutes(960);
                return routeArg;     // ← Devuelve el MISMO objeto
            });


        // when(legService).saveAllLegs(anyList(), any(Route.class));
        when(legService.saveAllLegs(anyList(), any(Route.class)))
            .thenReturn(legs);

        doNothing().when(packageClient).updatePackageStatus(any());

        // Step 4: Execute - should not throw capacity exceptions
        ShipmentResponseDTO result = shipmentService.createShipment(request);

        // Step 5: Assert
        assertThat(result).isNotNull();
        assertThat(result.routeId()).isEqualTo(1L);

        // Step 6: Verify the route was saved
        verify(routeService).save(any(Route.class));
        verify(legService).saveAllLegs(anyList(), any(Route.class));
    }
}