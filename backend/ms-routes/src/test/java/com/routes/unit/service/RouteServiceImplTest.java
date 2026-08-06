package com.routes.unit.service;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.exception.AppException;
import com.routes.mapper.RouteMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Route;
import com.routes.model.enums.RouteStatus;
import com.routes.repository.RouteRepository;
import com.routes.service.LegService;
import com.routes.service.impl.RouteServiceImpl;
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
@DisplayName("Route Service Unit Tests")
class RouteServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private RouteMapper routeMapper;

    @Mock
    private LegService legService;

    @InjectMocks
    private RouteServiceImpl routeService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private RouteRequestDTO request;
    private Route route;
    private Route savedRoute;
    private List<Leg> legs;
    private List<Leg> calculatedLegs;
    private List<Leg> savedLegs;
    private RouteDetailDTO response;

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

        // Step 2: Create leg request
        LegRequestDTO legRequest = new LegRequestDTO(
            1, 1L, 1L, origin, destination
        );

        // Step 3: Create route request
        request = new RouteRequestDTO(
            "Test Route",
            "Test description",
            List.of(legRequest)
        );

        // Step 4: Create route entity
        route = new Route();
        route.setName("Test Route");
        route.setDescription("Test description");

        // Step 5: Create saved route
        savedRoute = new Route();
        savedRoute.setId(1L);
        savedRoute.setName("Test Route");
        savedRoute.setStatus(RouteStatus.PLANNED);

        // Step 6: Create legs (in memory)
        Leg leg = new Leg();
        leg.setId(1L);
        leg.setSequence(1);
        leg.setDistanceKm(700.0);
        leg.setDurationMinutes(480);
        legs = List.of(leg);

        // Step 7: Create calculated legs
        calculatedLegs = List.of(leg);

        // Step 8: Create saved legs
        savedLegs = List.of(leg);

        // Step 9: Create response DTO
        response = new RouteDetailDTO(
            1L, "Test Route", "Test description", RouteStatus.PLANNED,
            700.0, 480, null, null, List.of()
        );
    }

    // ================================================================
    // TEST: GET ROUTE BY ID - Success
    // ================================================================

    @Test
    @DisplayName("Should return route by ID when it exists")
    void shouldReturnRouteById() {
        // STEP 1: Arrange
        Long routeId = 1L;
        Route route = new Route();
        route.setId(routeId);
        route.setName("Test Route");

        when(routeRepository.findByIdWithLegs(routeId))
            .thenReturn(Optional.of(route));
        when(routeMapper.toDetailDto(route)).thenReturn(response);

        // STEP 2: Act
        RouteDetailDTO result = routeService.getRouteById(routeId);

        // STEP 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(routeId);

        // STEP 4: Verify
        verify(routeRepository).findByIdWithLegs(routeId);
        verify(routeMapper).toDetailDto(route);
    }

    // ================================================================
    // TEST: GET ROUTE BY ID - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when route not found by ID")
    void shouldThrowExceptionWhenRouteNotFound() {
        // STEP 1: Arrange
        Long nonExistentId = 999L;
        when(routeRepository.findByIdWithLegs(nonExistentId))
            .thenReturn(Optional.empty());

        // STEP 2: Act & Assert
        assertThatThrownBy(() -> routeService.getRouteById(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Route not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // STEP 3: Verify
        verify(routeRepository).findByIdWithLegs(nonExistentId);
    }

    // ================================================================
    // TEST: UPDATE ROUTE - Success
    // ================================================================

    @Test
    @DisplayName("Should update route successfully with new legs")
    void shouldUpdateRoute() {
        // STEP 1: Arrange
        Long routeId = 1L;
        Route existingRoute = new Route();
        existingRoute.setId(routeId);
        existingRoute.setName("Old Name");
        existingRoute.setDescription("Old Description");

        Route updatedRoute = new Route();
        updatedRoute.setId(routeId);
        updatedRoute.setName(request.name());
        updatedRoute.setDescription(request.description());

        when(routeRepository.findByIdWithLegs(routeId))
            .thenReturn(Optional.of(existingRoute));
        when(routeRepository.save(existingRoute)).thenReturn(updatedRoute);
        when(legService.createLegsInMemory(request.legs())).thenReturn(legs);

        // old
        // when(geocodingService.calculateLegDistances(legs)).thenReturn(calculatedLegs);
        when(legService.saveAllLegs(calculatedLegs, updatedRoute)).thenReturn(savedLegs);
        when(routeMapper.toDetailDto(updatedRoute)).thenReturn(response);

        // STEP 2: Act
        RouteDetailDTO result = routeService.updateRoute(routeId, request);

        // STEP 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(routeId);

        // STEP 4: Verify
        verify(routeRepository).findByIdWithLegs(routeId);
        verify(routeRepository).save(existingRoute);
        verify(legService).createLegsInMemory(request.legs());

        // old
        // verify(geocodingService).calculateLegDistances(legs);
        verify(legService).saveAllLegs(calculatedLegs, updatedRoute);
    }

    // ================================================================
    // TEST: UPDATE ROUTE - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when updating non-existent route")
    void shouldThrowExceptionWhenUpdatingNonExistentRoute() {
        // STEP 1: Arrange
        Long nonExistentId = 999L;
        when(routeRepository.findByIdWithLegs(nonExistentId))
            .thenReturn(Optional.empty());

        // STEP 2: Act & Assert
        assertThatThrownBy(() -> routeService.updateRoute(nonExistentId, request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Route not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);
    }

    // ================================================================
    // TEST: DELETE ROUTE - Success
    // ================================================================

    @Test
    @DisplayName("Should delete route successfully")
    void shouldDeleteRoute() {
        // STEP 1: Arrange
        Long routeId = 1L;
        when(routeRepository.findById(routeId))
            .thenReturn(Optional.of(savedRoute));

        // STEP 2: Act
        routeService.deleteRoute(routeId);

        // STEP 3: Verify
        verify(routeRepository).delete(savedRoute);
    }

    // ================================================================
    // TEST: DELETE ROUTE - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when deleting non-existent route")
    void shouldThrowExceptionWhenDeletingNonExistentRoute() {
        // STEP 1: Arrange
        Long nonExistentId = 999L;
        when(routeRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        // STEP 2: Act & Assert
        assertThatThrownBy(() -> routeService.deleteRoute(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Route not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);
    }
}