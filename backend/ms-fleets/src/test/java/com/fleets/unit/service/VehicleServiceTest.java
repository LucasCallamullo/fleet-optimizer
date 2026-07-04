package com.fleets.unit.service;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleDetailDTO;
import com.fleets.dto.response.CategoryResponseDTO;
import com.fleets.exception.AppException;
import com.fleets.mapper.VehicleMapper;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.model.VehicleStatus;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;
import com.fleets.service.CategoryService;
import com.fleets.service.impl.VehicleServiceImpl;
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

/**
 * Unit tests for VehicleServiceImpl.
 * 
 * All dependencies are mocked using Mockito.
 * No real database is used - tests are fast and isolated.
 * 
 * This test class validates:
 * - Business logic
 * - Exception handling
 * - Data mapping
 * - Transactional behavior
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Vehicle Service Unit Tests")
class VehicleServiceTest {

    // ================================================================
    // MOCKED DEPENDENCIES - Simulated implementations
    // ================================================================

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private VehicleMapper vehicleMapper;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    // ================================================================
    // TEST DATA - Shared across all tests
    // ================================================================

    private Vehicle vehicle;
    private Category category;
    private VehicleRequestDTO request;
    private VehicleDetailDTO response;
    private CategoryResponseDTO categoryResponse;

    // ================================================================
    // SETUP - Executes before each test
    // ================================================================

    @BeforeEach
    void setUp() {
        // Step 1: Create category entity
        category = new Category();
        category.setId(1L);
        category.setName("Sedan");

        // Step 2: Create vehicle entity
        vehicle = new Vehicle();
        vehicle.setId(10L);
        vehicle.setLicensePlate("ABC123");
        vehicle.setYear(2023);
        vehicle.setCategory(category);

        // Step 3: Create request DTO
        request = new VehicleRequestDTO();
        request.setLicensePlate("ABC123");
        request.setYear(2023);
        request.setCategoryId(1L);

        // Step 4: Create category response DTO (Record)
        categoryResponse = new CategoryResponseDTO(
            1L,
            "Sedan",
            "some description"
        );

        // Step 5: Create vehicle detail response DTO (Record)
        response = new VehicleDetailDTO(
            10L,
            "ABC123",
            2023,
            LocalDateTime.now(),
            LocalDateTime.now(),
            categoryResponse,
            null,
            null,
            null,
            null,
            null,
            VehicleStatus.AVAILABLE
        );
    }

    // ================================================================
    // TEST 1: GET ALL VEHICLES
    // ================================================================

    /**
     * Test: getAllVehiclesWithCategory()
     * 
     * Verifies that the service returns all vehicles with their categories.
     */
    @Test
    @DisplayName("Should return all vehicles as DTOs")
    void shouldReturnAllVehicles() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Create list of vehicles (contains 1 vehicle)
        List<Vehicle> vehicles = List.of(vehicle);
        
        // 1.2: Configure mocks
        // When vehicleRepository.findAllWithCategory() is called, return the vehicles list
        when(vehicleRepository.findAllWithCategory()).thenReturn(vehicles);
        
        // When vehicleMapper.toDetailDto() is called with any Vehicle, return the response
        when(vehicleMapper.toDetailDto(any(Vehicle.class))).thenReturn(response);

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method
        List<VehicleDetailDTO> result = vehicleService.getAllVehiclesWithCategory();

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is not null
        assertThat(result).isNotNull();
        
        // 3.2: Verify result has exactly 1 element
        assertThat(result).hasSize(1);
        
        // 3.3: Verify the vehicle data is correct
        assertThat(result.get(0).licensePlate()).isEqualTo("ABC123");
        assertThat(result.get(0).year()).isEqualTo(2023);
        assertThat(result.get(0).category().id()).isEqualTo(1L);
        assertThat(result.get(0).category().name()).isEqualTo("Sedan");

        // STEP 4: Verify - Ensure the mocked methods were called correctly
        // 4.1: Verify repository was called once
        verify(vehicleRepository).findAllWithCategory();
        
        // 4.2: Verify mapper was called once
        verify(vehicleMapper).toDetailDto(any(Vehicle.class));
    }

    // ================================================================
    // TEST 2: GET ALL VEHICLES ENTITY
    // ================================================================

    /**
     * Test: getAllVehiclesEntity()
     * 
     * Verifies that the service returns all vehicles as entities.
     * This is an internal method used by other service methods.
     */
    @Test
    @DisplayName("Should return all vehicles as entities")
    void shouldReturnAllVehiclesEntity() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Create list of vehicles (contains 1 vehicle)
        List<Vehicle> vehicles = List.of(vehicle);
        
        // 1.2: Configure mock
        // When vehicleRepository.findAll() is called, return the vehicles list
        when(vehicleRepository.findAll()).thenReturn(vehicles);

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method
        List<Vehicle> result = vehicleService.getAllVehiclesEntity();

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is not null
        assertThat(result).isNotNull();
        
        // 3.2: Verify result has exactly 1 element
        assertThat(result).hasSize(1);
        
        // 3.3: Verify the vehicle data is correct
        assertThat(result.get(0).getId()).isEqualTo(10L);
        assertThat(result.get(0).getLicensePlate()).isEqualTo("ABC123");
        assertThat(result.get(0).getYear()).isEqualTo(2023);
        assertThat(result.get(0).getCategory().getId()).isEqualTo(1L);
        assertThat(result.get(0).getCategory().getName()).isEqualTo("Sedan");

        // STEP 4: Verify - Ensure the mocked method was called correctly
        // 4.1: Verify repository was called once
        verify(vehicleRepository).findAll();
    }

    // ================================================================
    // TEST 3: GET VEHICLE ENTITY BY ID - Success
    // ================================================================

    /**
     * Test: getVehicleEntityById()
     * 
     * Verifies that the service returns a vehicle entity when it exists.
     * This is an internal method used by other service methods.
     */
    @Test
    @DisplayName("Should return vehicle entity by ID when it exists")
    void shouldReturnVehicleEntityById() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define the ID we're searching for
        Long vehicleId = 10L;

        // 1.2: Configure mock
        // When vehicleRepository.findById() is called with this ID, return the vehicle wrapped in Optional
        when(vehicleRepository.findById(vehicleId))
            .thenReturn(Optional.of(vehicle));

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method with the valid ID
        Vehicle result = vehicleService.getVehicleEntityById(vehicleId);

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is not null
        assertThat(result).isNotNull();
        
        // 3.2: Verify all vehicle data is correct
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getLicensePlate()).isEqualTo("ABC123");
        assertThat(result.getYear()).isEqualTo(2023);
        assertThat(result.getCategory().getId()).isEqualTo(1L);

        // STEP 4: Verify - Ensure the mocked method was called correctly
        // 4.1: Verify repository was called once with the correct ID
        verify(vehicleRepository).findById(vehicleId);
    }

    // ================================================================
    // TEST 4: GET VEHICLE ENTITY BY ID - Not Found
    // ================================================================

    /**
     * Test: getVehicleEntityById()
     * 
     * Verifies that the service throws an exception when the vehicle does not exist.
     */
    @Test
    @DisplayName("Should throw exception when vehicle entity not found by ID")
    void shouldThrowExceptionWhenVehicleEntityNotFound() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define a non-existent ID
        Long nonExistentId = 999L;

        // 1.2: Configure mock
        // When vehicleRepository.findById() is called with this ID, return empty Optional
        when(vehicleRepository.findById(nonExistentId))
            .thenReturn(Optional.empty());

        // STEP 2: Act & Assert - Execute and verify exception
        // 2.1: Verify that calling the method throws AppException with correct message and statusCode
        assertThatThrownBy(() -> vehicleService.getVehicleEntityById(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Vehicle not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // STEP 3: Verify - Ensure the mocked method was called correctly
        // 3.1: Verify repository was called once with the correct ID
        verify(vehicleRepository).findById(nonExistentId);
    }

    // ================================================================
    // TEST 5: GET VEHICLE ENTITY WITH CATEGORY BY ID - Success
    // ================================================================

    /**
     * Test: getVehicleEntityWithCategoryById()
     * 
     * Verifies that the service returns a vehicle entity with its category
     * loaded (JOIN FETCH) when it exists.
     * This is an internal method used by other service methods.
     */
    @Test
    @DisplayName("Should return vehicle entity with category by ID when it exists")
    void shouldReturnVehicleEntityWithCategoryById() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define the ID we're searching for
        Long vehicleId = 10L;

        // 1.2: Configure mock
        // When vehicleRepository.findByIdWithCategory() is called, return the vehicle with category
        when(vehicleRepository.findByIdWithCategory(vehicleId))
            .thenReturn(Optional.of(vehicle));

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method with the valid ID
        Vehicle result = vehicleService.getVehicleEntityWithCategoryById(vehicleId);

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is not null
        assertThat(result).isNotNull();
        
        // 3.2: Verify all vehicle data is correct
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getLicensePlate()).isEqualTo("ABC123");
        assertThat(result.getYear()).isEqualTo(2023);
        
        // 3.3: Verify category data is loaded (JOIN FETCH worked)
        assertThat(result.getCategory()).isNotNull();
        assertThat(result.getCategory().getId()).isEqualTo(1L);
        assertThat(result.getCategory().getName()).isEqualTo("Sedan");

        // STEP 4: Verify - Ensure the mocked method was called correctly
        // 4.1: Verify repository was called once with the correct ID
        verify(vehicleRepository).findByIdWithCategory(vehicleId);
    }

    // ================================================================
    // TEST 6: GET VEHICLE ENTITY WITH CATEGORY BY ID - Not Found
    // ================================================================

    /**
     * Test: getVehicleEntityWithCategoryById()
     * 
     * Verifies that the service throws an exception when the vehicle does not exist.
     */
    @Test
    @DisplayName("Should throw exception when vehicle entity with category not found by ID")
    void shouldThrowExceptionWhenVehicleEntityWithCategoryNotFound() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define a non-existent ID
        Long nonExistentId = 999L;

        // 1.2: Configure mock
        // When vehicleRepository.findByIdWithCategory() is called, return empty Optional
        when(vehicleRepository.findByIdWithCategory(nonExistentId))
            .thenReturn(Optional.empty());

        // STEP 2: Act & Assert - Execute and verify exception
        // 2.1: Verify that calling the method throws AppException with correct message and statusCode
        assertThatThrownBy(() -> vehicleService.getVehicleEntityWithCategoryById(nonExistentId))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Vehicle not found with id: 999")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // STEP 3: Verify - Ensure the mocked method was called correctly
        // 3.1: Verify repository was called once with the correct ID
        verify(vehicleRepository).findByIdWithCategory(nonExistentId);
    }

    // ================================================================
    // TEST 7: GET VEHICLE ENTITY BY LICENSE PLATE - Success
    // ================================================================

    /**
     * Test: getVehicleEntityByLicensePlate()
     * 
     * Verifies that the service returns a vehicle entity when found by license plate.
     * This is an internal method used by other service methods.
     */
    @Test
    @DisplayName("Should return vehicle entity by license plate when it exists")
    void shouldReturnVehicleEntityByLicensePlate() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define the license plate we're searching for
        String licensePlate = "ABC123";

        // 1.2: Configure mock
        // When vehicleRepository.findByLicensePlate() is called, return the vehicle wrapped in Optional
        when(vehicleRepository.findByLicensePlate(licensePlate))
            .thenReturn(Optional.of(vehicle));

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method with the valid license plate
        Optional<Vehicle> result = vehicleService.getVehicleEntityByLicensePlate(licensePlate);

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is present (not empty)
        assertThat(result).isPresent();
        
        // 3.2: Verify the vehicle data is correct
        assertThat(result.get().getId()).isEqualTo(10L);
        assertThat(result.get().getLicensePlate()).isEqualTo("ABC123");
        assertThat(result.get().getYear()).isEqualTo(2023);
        assertThat(result.get().getCategory().getId()).isEqualTo(1L);

        // STEP 4: Verify - Ensure the mocked method was called correctly
        // 4.1: Verify repository was called once with the correct license plate
        verify(vehicleRepository).findByLicensePlate(licensePlate);
    }

    // ================================================================
    // TEST 8: GET VEHICLE ENTITY BY LICENSE PLATE - Not Found
    // ================================================================

    /**
     * Test: getVehicleEntityByLicensePlate()
     * 
     * Verifies that the service returns empty Optional when the vehicle does not exist.
     */
    @Test
    @DisplayName("Should return empty Optional when vehicle entity not found by license plate")
    void shouldReturnEmptyOptionalWhenVehicleEntityNotFoundByLicensePlate() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define a non-existent license plate
        String nonExistentPlate = "NONEXISTENT";

        // 1.2: Configure mock
        // When vehicleRepository.findByLicensePlate() is called, return empty Optional
        when(vehicleRepository.findByLicensePlate(nonExistentPlate))
            .thenReturn(Optional.empty());

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method with the non-existent license plate
        Optional<Vehicle> result = vehicleService.getVehicleEntityByLicensePlate(nonExistentPlate);

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is empty (not present)
        assertThat(result).isEmpty();

        // STEP 4: Verify - Ensure the mocked method was called correctly
        // 4.1: Verify repository was called once with the correct license plate
        verify(vehicleRepository).findByLicensePlate(nonExistentPlate);
    }

    // ================================================================
    // TEST 9: GET VEHICLE DTO BY ID
    // ================================================================

    /**
     * Test: getVehicleById()
     * 
     * Verifies that the service returns a vehicle DTO with category when it exists.
     * This method uses getVehicleEntityWithCategoryById() internally.
     */
    @Test
    @DisplayName("Should return vehicle DTO by ID when it exists")
    void shouldReturnVehicleDtoById() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define the ID we're searching for
        Long vehicleId = 10L;

        // 1.2: Configure mocks
        // When vehicleRepository.findByIdWithCategory() is called, return the vehicle with category
        when(vehicleRepository.findByIdWithCategory(vehicleId))
            .thenReturn(Optional.of(vehicle));
        
        // When vehicleMapper.toDetailDto() is called with this vehicle, return the response DTO
        when(vehicleMapper.toDetailDto(vehicle)).thenReturn(response);

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method with the valid ID
        VehicleDetailDTO result = vehicleService.getVehicleById(vehicleId);

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is not null
        assertThat(result).isNotNull();
        
        // 3.2: Verify all vehicle data is correct
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.licensePlate()).isEqualTo("ABC123");
        assertThat(result.year()).isEqualTo(2023);
        
        // 3.3: Verify category data is correct
        assertThat(result.category()).isNotNull();
        assertThat(result.category().id()).isEqualTo(1L);
        assertThat(result.category().name()).isEqualTo("Sedan");

        // STEP 4: Verify - Ensure the mocked methods were called correctly
        // 4.1: Verify repository was called once
        verify(vehicleRepository).findByIdWithCategory(vehicleId);
        
        // 4.2: Verify mapper was called once
        verify(vehicleMapper).toDetailDto(vehicle);
    }

    // ================================================================
    // TEST 10: GET VEHICLE DTO BY LICENSE PLATE - Success
    // ================================================================

    /**
     * Test: getVehicleByLicensePlate()
     * 
     * Verifies that the service returns a vehicle DTO when found by license plate.
     */
    @Test
    @DisplayName("Should return vehicle DTO by license plate when it exists")
    void shouldReturnVehicleDtoByLicensePlate() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define the license plate we're searching for
        String licensePlate = "ABC123";

        // 1.2: Configure mocks
        // When vehicleRepository.findByLicensePlateWithCategory() is called, return the vehicle with category
        when(vehicleRepository.findByLicensePlateWithCategory(licensePlate))
            .thenReturn(Optional.of(vehicle));
        
        // When vehicleMapper.toDetailDto() is called with this vehicle, return the response DTO
        when(vehicleMapper.toDetailDto(vehicle)).thenReturn(response);

        // STEP 2: Act - Execute the method being tested
        // 2.1: Call the service method with the valid license plate
        VehicleDetailDTO result = vehicleService.getVehicleByLicensePlate(licensePlate);

        // STEP 3: Assert - Verify the results
        // 3.1: Verify result is not null
        assertThat(result).isNotNull();
        
        // 3.2: Verify all vehicle data is correct
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.licensePlate()).isEqualTo("ABC123");
        assertThat(result.year()).isEqualTo(2023);
        
        // 3.3: Verify category data is correct
        assertThat(result.category()).isNotNull();
        assertThat(result.category().id()).isEqualTo(1L);
        assertThat(result.category().name()).isEqualTo("Sedan");

        // STEP 4: Verify - Ensure the mocked methods were called correctly
        // 4.1: Verify repository was called once with the correct license plate
        verify(vehicleRepository).findByLicensePlateWithCategory(licensePlate);
        
        // 4.2: Verify mapper was called once
        verify(vehicleMapper).toDetailDto(vehicle);
    }

    // ================================================================
    // TEST 11: GET VEHICLE DTO BY LICENSE PLATE - Not Found
    // ================================================================

    /**
     * Test: getVehicleByLicensePlate()
     * 
     * Verifies that the service throws an exception when the vehicle does not exist.
     */
    @Test
    @DisplayName("Should throw exception when vehicle DTO not found by license plate")
    void shouldThrowExceptionWhenVehicleDtoNotFoundByLicensePlate() {
        // STEP 1: Arrange - Setup test data and mocks
        // 1.1: Define a non-existent license plate
        String nonExistentPlate = "NONEXISTENT";

        // 1.2: Configure mock
        // When vehicleRepository.findByLicensePlateWithCategory() is called, return empty Optional
        when(vehicleRepository.findByLicensePlateWithCategory(nonExistentPlate))
            .thenReturn(Optional.empty());

        // STEP 2: Act & Assert - Execute and verify exception
        // 2.1: Verify that calling the method throws AppException with correct message and statusCode
        assertThatThrownBy(() -> vehicleService.getVehicleByLicensePlate(nonExistentPlate))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("Vehicle not found with license plate: NONEXISTENT")
            .hasFieldOrPropertyWithValue("statusCode", 404);

        // STEP 3: Verify - Ensure the mocked method was called correctly
        // 3.1: Verify repository was called once with the correct license plate
        verify(vehicleRepository).findByLicensePlateWithCategory(nonExistentPlate);
        
        // 3.2: Verify mapper was never called (because vehicle wasn't found)
        verify(vehicleMapper, never()).toDetailDto(any(Vehicle.class));
    }

    
    // ================================================================
    // HELPER METHODS - Factory para crear vehículos de prueba
    // ================================================================

    /**
     * Creates a complete test scenario with all objects needed for createVehicle tests.
     * 
     * @param categoryId The category ID to use (null for no category)
     * @return A TestScenario object containing all test data
     */
    private TestScenario createTestScenario(Long categoryId) {
        // Step 1: Create request
        VehicleRequestDTO request = new VehicleRequestDTO();
        request.setLicensePlate("ABC123");
        request.setYear(2023);
        request.setCategoryId(categoryId);
        
        // Step 2: Create category (if provided)
        Category category = null;
        CategoryResponseDTO categoryResponse = null;
        if (categoryId != null) {
            category = new Category();
            category.setId(categoryId);
            category.setName("Sedan");
            categoryResponse = new CategoryResponseDTO(categoryId, "Sedan", "stupid description");
        }
        
        // Step 3: Create vehicle to save (without ID)
        Vehicle vehicleToSave = new Vehicle();
        vehicleToSave.setLicensePlate("ABC123");
        vehicleToSave.setYear(2023);
        vehicleToSave.setCategory(category);
        
        // Step 4: Create saved vehicle (with ID)
        Vehicle savedVehicle = new Vehicle();
        savedVehicle.setId(10L);
        savedVehicle.setLicensePlate("ABC123");
        savedVehicle.setYear(2023);
        savedVehicle.setCategory(category);
        
        // Step 5: Create expected response
        VehicleDetailDTO expectedResponse = new VehicleDetailDTO(
            10L,
            "ABC123",
            2023,
            LocalDateTime.now(),
            LocalDateTime.now(),
            categoryResponse,
            null,
            null,
            null,
            null,
            null,
            VehicleStatus.AVAILABLE
        );
        
        return new TestScenario(request, vehicleToSave, savedVehicle, expectedResponse, category);
    }

    /**
     * Inner class to hold all test data for a scenario.
     */
    private record TestScenario(
        VehicleRequestDTO request,
        Vehicle vehicleToSave,
        Vehicle savedVehicle,
        VehicleDetailDTO expectedResponse,
        Category category
    ) {}

    @Test
    @DisplayName("Should handle null category when creating vehicle")
    void shouldHandleNullCategory() {
        // ============================================================
        // STEP 1: Arrange - Create test scenario without category
        // ============================================================
        
        TestScenario scenario = createTestScenario(null);  // ← NULL category
        
        // Configure mocks
        when(vehicleRepository.findByLicensePlate("ABC123"))
            .thenReturn(Optional.empty());
        when(vehicleMapper.toEntity(scenario.request))
            .thenReturn(scenario.vehicleToSave);
        when(vehicleRepository.save(any(Vehicle.class)))
            .thenReturn(scenario.savedVehicle);
        when(vehicleMapper.toDetailDto(scenario.savedVehicle))
            .thenReturn(scenario.expectedResponse);

        // ============================================================
        // STEP 2: Act
        // ============================================================
        
        VehicleDetailDTO result = vehicleService.createVehicle(scenario.request);

        // ============================================================
        // STEP 3: Assert
        // ============================================================
        
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.licensePlate()).isEqualTo("ABC123");
        assertThat(result.year()).isEqualTo(2023);
        assertThat(result.category()).isNull();  // ← Verifica null

        // ============================================================
        // STEP 4: Verify
        // ============================================================
        
        verify(vehicleRepository).findByLicensePlate("ABC123");
        verify(categoryService, never()).getCategoryEntityById(anyLong());
        verify(vehicleMapper).toEntity(scenario.request);
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(vehicleMapper).toDetailDto(scenario.savedVehicle);
    }

    @Test
    @DisplayName("Should set category when provided")
    void shouldSetCategoryWhenProvided() {
        // ============================================================
        // STEP 1: Arrange - Create test scenario WITH category
        // ============================================================
        
        TestScenario scenario = createTestScenario(1L);  // ← WITH category
        
        // Configure mocks
        when(vehicleRepository.findByLicensePlate("ABC123"))
            .thenReturn(Optional.empty());
        when(categoryService.getCategoryEntityById(1L))
            .thenReturn(scenario.category);
        when(vehicleMapper.toEntity(scenario.request))
            .thenReturn(scenario.vehicleToSave);
        when(vehicleRepository.save(any(Vehicle.class)))
            .thenReturn(scenario.savedVehicle);
        when(vehicleMapper.toDetailDto(scenario.savedVehicle))
            .thenReturn(scenario.expectedResponse);

        // ============================================================
        // STEP 2: Act
        // ============================================================
        
        VehicleDetailDTO result = vehicleService.createVehicle(scenario.request);

        // ============================================================
        // STEP 3: Assert
        // ============================================================
        
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.licensePlate()).isEqualTo("ABC123");
        assertThat(result.year()).isEqualTo(2023);
        assertThat(result.category()).isNotNull();
        assertThat(result.category().id()).isEqualTo(1L);
        assertThat(result.category().name()).isEqualTo("Sedan");

        // ============================================================
        // STEP 4: Verify
        // ============================================================
        
        verify(vehicleRepository).findByLicensePlate("ABC123");
        verify(categoryService).getCategoryEntityById(1L);
        verify(vehicleMapper).toEntity(scenario.request);
        verify(vehicleRepository).save(any(Vehicle.class));
        verify(vehicleMapper).toDetailDto(scenario.savedVehicle);
    }
}