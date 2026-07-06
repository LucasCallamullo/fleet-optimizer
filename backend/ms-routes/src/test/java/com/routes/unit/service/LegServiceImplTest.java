package com.routes.unit.service;

import com.routes.dto.request.LegRequestDTO;
import com.routes.dto.request.LocationRequestDTO;
import com.routes.dto.response.LegDetailDTO;
import com.routes.exception.AppException;
import com.routes.mapper.RouteMapper;
import com.routes.model.entity.Leg;
import com.routes.model.entity.Location;
import com.routes.model.entity.Route;
import com.routes.model.enums.LegStatus;
import com.routes.repository.LegRepository;
import com.routes.service.impl.LegServiceImpl;
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
@DisplayName("Leg Service Unit Tests")
class LegServiceImplTest {

    @Mock
    private LegRepository legRepository;

    @Mock
    private RouteMapper routeMapper;

    @InjectMocks
    private LegServiceImpl legService;

    // ================================================================
    // TEST DATA
    // ================================================================

    private Route route;
    private Leg leg;
    private Leg savedLeg;
    private LegRequestDTO legRequest;
    private LegDetailDTO legDetailDTO;
    private Location origin;
    private Location destination;
    private List<LegRequestDTO> legRequests;
    private List<Leg> legs;

    @BeforeEach
    void setUp() {
        // Step 1: Create route
        route = new Route();
        route.setId(1L);
        route.setName("Test Route");

        // Step 2: Create locations
        origin = new Location();
        origin.setStreet("Av. Libertador");
        origin.setStreetNumber("1000");
        origin.setCity("Buenos Aires");
        origin.setLatitude(-34.6037);
        origin.setLongitude(-58.3816);

        destination = new Location();
        destination.setStreet("Av. Colon");
        destination.setStreetNumber("500");
        destination.setCity("Cordoba");
        destination.setLatitude(-31.4201);
        destination.setLongitude(-64.1888);

        // Step 3: Create location DTOs
        LocationRequestDTO originDTO = new LocationRequestDTO(
            "Av. Libertador", "1000", "Buenos Aires", "CABA",
            "Argentina", "1000", -34.6037, -58.3816
        );

        LocationRequestDTO destDTO = new LocationRequestDTO(
            "Av. Colon", "500", "Cordoba", "Cordoba",
            "Argentina", "5000", -31.4201, -64.1888
        );

        // Step 4: Create leg request
        legRequest = new LegRequestDTO(
            1, 10L, 100L, originDTO, destDTO
        );

        legRequests = List.of(legRequest);

        // Step 5: Create leg entity
        leg = new Leg();
        leg.setSequence(1);
        leg.setVehicleId(10L);
        leg.setPackageId(100L);
        leg.setOrigin(origin);
        leg.setDestination(destination);
        leg.setStatus(LegStatus.PENDING);

        // Step 6: Create saved leg (with ID and route)
        savedLeg = new Leg();
        savedLeg.setId(1L);
        savedLeg.setSequence(1);
        savedLeg.setVehicleId(10L);
        savedLeg.setPackageId(100L);
        savedLeg.setOrigin(origin);
        savedLeg.setDestination(destination);
        savedLeg.setStatus(LegStatus.PENDING);
        savedLeg.setRoute(route);

        // Step 7: Create response DTO
        legDetailDTO = new LegDetailDTO(
            1L, 1, LegStatus.PENDING, 700.0, 480,
            null, null, null, null,
            10L, 100L, null, null
        );

        legs = List.of(leg);
    }

    // ================================================================
    // TEST: CREATE LEGS IN MEMORY
    // ================================================================

    @Test
    @DisplayName("Should create legs in memory without persisting")
    void shouldCreateLegsInMemory() {
        // Step 1: Mock mapper
        when(routeMapper.toLegEntity(legRequest)).thenReturn(leg);

        // Step 2: Act
        List<Leg> result = legService.createLegsInMemory(legRequests);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSequence()).isEqualTo(1);
        assertThat(result.get(0).getStatus()).isEqualTo(LegStatus.PENDING);
        assertThat(result.get(0).getRoute()).isNull(); // Route not set yet

        // Step 4: Verify
        verify(routeMapper).toLegEntity(legRequest);
        verify(legRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: SAVE ALL LEGS - Success
    // ================================================================

    @Test
    @DisplayName("Should save all legs with route association")
    void shouldSaveAllLegs() {
        // Step 1: Mock
        when(legRepository.saveAll(legs)).thenReturn(List.of(savedLeg));

        // Step 2: Act
        List<Leg> result = legService.saveAllLegs(legs, route);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getRoute()).isNotNull();
        assertThat(result.get(0).getRoute().getId()).isEqualTo(1L);

        // Step 4: Verify
        verify(legRepository).saveAll(legs);
    }

    // ================================================================
    // TEST: SAVE ALL LEGS - Empty list
    // ================================================================

    @Test
    @DisplayName("Should throw exception when saving empty legs list")
    void shouldThrowExceptionWhenSavingEmptyLegs() {
        // Step 1: Act & Assert
        assertThatThrownBy(() -> legService.saveAllLegs(List.of(), route))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Cannot save legs: at least one leg is required")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 2: Verify
        verify(legRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: SAVE ALL LEGS - Without route
    // ================================================================

    @Test
    @DisplayName("Should throw exception when saving legs without route")
    void shouldThrowExceptionWhenSavingLegsWithoutRoute() {
        // Step 1: Act & Assert
        assertThatThrownBy(() -> legService.saveAllLegs(legs, null))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Cannot save legs without an associated route")
            .hasFieldOrPropertyWithValue("statusCode", 400);

        // Step 2: Verify
        verify(legRepository, never()).saveAll(any());
    }

    // ================================================================
    // TEST: GET LEG ENTITY BY ID - Success
    // ================================================================

    @Test
    @DisplayName("Should return leg entity by ID when it exists")
    void shouldReturnLegEntityById() {
        // Step 1: Arrange
        Long legId = 1L;
        when(legRepository.findById(legId)).thenReturn(Optional.of(savedLeg));

        // Step 2: Act
        Leg result = legService.getLegEntityById(legId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getSequence()).isEqualTo(1);

        // Step 4: Verify
        verify(legRepository).findById(legId);
    }

    // ================================================================
    // TEST: GET LEG ENTITY BY ID - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when leg not found by ID")
    void shouldThrowExceptionWhenLegNotFound() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(legRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> legService.getLegEntityById(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Leg not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(legRepository).findById(nonExistentId);
    }

    // ================================================================
    // TEST: GET LEG DTO BY ID - Success
    // ================================================================

    @Test
    @DisplayName("Should return leg DTO by ID when it exists")
    void shouldReturnLegDtoById() {
        // Step 1: Arrange
        Long legId = 1L;
        when(legRepository.findById(legId)).thenReturn(Optional.of(savedLeg));
        when(routeMapper.toLegDetailDto(savedLeg)).thenReturn(legDetailDTO);

        // Step 2: Act
        LegDetailDTO result = legService.getLegById(legId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);

        // Step 4: Verify
        verify(legRepository).findById(legId);
        verify(routeMapper).toLegDetailDto(savedLeg);
    }

    // ================================================================
    // TEST: GET LEG DTO BY ID - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when leg DTO not found by ID")
    void shouldThrowExceptionWhenLegDtoNotFound() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(legRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> legService.getLegById(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Leg not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(legRepository).findById(nonExistentId);
        verify(routeMapper, never()).toLegDetailDto(any());
    }

    // ================================================================
    // TEST: GET LEGS BY ROUTE ID
    // ================================================================

    @Test
    @DisplayName("Should return legs by route ID")
    void shouldReturnLegsByRouteId() {
        // Step 1: Arrange
        Long routeId = 1L;
        when(legRepository.findByRouteId(routeId)).thenReturn(List.of(savedLeg));
        when(routeMapper.toLegDetailDto(savedLeg)).thenReturn(legDetailDTO);

        // Step 2: Act
        List<LegDetailDTO> result = legService.getLegsByRouteId(routeId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(1L);

        // Step 4: Verify
        verify(legRepository).findByRouteId(routeId);
        verify(routeMapper).toLegDetailDto(savedLeg);
    }

    // ================================================================
    // TEST: GET LEGS BY ROUTE ID - Empty
    // ================================================================

    @Test
    @DisplayName("Should return empty list when route has no legs")
    void shouldReturnEmptyListWhenRouteHasNoLegs() {
        // Step 1: Arrange
        Long routeId = 1L;
        when(legRepository.findByRouteId(routeId)).thenReturn(List.of());

        // Step 2: Act
        List<LegDetailDTO> result = legService.getLegsByRouteId(routeId);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();

        // Step 4: Verify
        verify(legRepository).findByRouteId(routeId);
        verify(routeMapper, never()).toLegDetailDto(any());
    }

    // ================================================================
    // TEST: UPDATE LEG - Success
    // ================================================================

    @Test
    @DisplayName("Should update leg successfully")
    void shouldUpdateLeg() {
        // Step 1: Arrange
        Long legId = 1L;
        when(legRepository.findById(legId)).thenReturn(Optional.of(savedLeg));
        when(routeMapper.toLocationEntity(legRequest.origin())).thenReturn(origin);
        when(routeMapper.toLocationEntity(legRequest.destination())).thenReturn(destination);
        when(legRepository.save(savedLeg)).thenReturn(savedLeg);
        when(routeMapper.toLegDetailDto(savedLeg)).thenReturn(legDetailDTO);

        // Step 2: Act
        LegDetailDTO result = legService.updateLeg(legId, legRequest);

        // Step 3: Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);

        // Step 4: Verify
        verify(legRepository).findById(legId);
        verify(legRepository).save(savedLeg);
        verify(routeMapper).toLegDetailDto(savedLeg);
    }

    // ================================================================
    // TEST: UPDATE LEG - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when updating non-existent leg")
    void shouldThrowExceptionWhenUpdatingNonExistentLeg() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(legRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> legService.updateLeg(nonExistentId, legRequest))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Leg not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(legRepository).findById(nonExistentId);
        verify(legRepository, never()).save(any());
    }

    // ================================================================
    // TEST: DELETE LEG - Success
    // ================================================================

    @Test
    @DisplayName("Should delete leg successfully")
    void shouldDeleteLeg() {
        // Step 1: Arrange
        Long legId = 1L;
        when(legRepository.findById(legId)).thenReturn(Optional.of(savedLeg));

        // Step 2: Act
        legService.deleteLeg(legId);

        // Step 3: Verify
        verify(legRepository).findById(legId);
        verify(legRepository).delete(savedLeg);
    }

    // ================================================================
    // TEST: DELETE LEG - Not Found
    // ================================================================

    @Test
    @DisplayName("Should throw exception when deleting non-existent leg")
    void shouldThrowExceptionWhenDeletingNonExistentLeg() {
        // Step 1: Arrange
        Long nonExistentId = 999L;
        when(legRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Step 2: Act & Assert
        assertThatThrownBy(() -> legService.deleteLeg(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Leg not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // Step 3: Verify
        verify(legRepository).findById(nonExistentId);
        verify(legRepository, never()).delete(any());
    }
}