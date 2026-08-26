package com.fleets.service.impl;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleResponseDTO;
import com.fleets.dto.response.VehicleDetailDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.model.VehicleStatus;
import com.fleets.repository.VehicleRepository;
import com.fleets.service.CategoryService;
import com.fleets.service.VehicleService;
import com.fleets.mapper.VehicleMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * JPA-based implementation of VehicleService.
 * Uses Spring Data JPA for database operations.
 * 
 * ================================================================
 * PATTERNS USED:
 * 1. Repository Pattern - Data access abstraction
 * 2. DTO Pattern - Data transfer between layers
 * 3. Mapper Pattern - Entity ↔ DTO conversion
 * 4. Transactional - Database consistency
 * 5. Exception Handling - Business rule validation
 * 
 * ================================================================
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VehicleServiceImpl implements VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final CategoryService categoryService;
    private final VehicleMapper vehicleMapper;
    
    // ================================================================
    // ENTITY METHODS - For internal use
    // ================================================================
    
    @Override
    public List<Vehicle> getAllVehiclesEntity() {
        log.debug("Fetching all vehicles from database");
        List<Vehicle> vehicles = vehicleRepository.findAll();
        log.debug("Found {} vehicles in database", vehicles.size());
        return vehicles;
    }

    @Override
    public Vehicle getVehicleEntityById(Long id) {
        log.debug("Fetching vehicle entity by id: {}", id);
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new AppException("Vehicle not found with id: " + id, 404));
    }

    @Override
    public Vehicle getVehicleEntityWithCategoryById(Long id) {
        log.debug("Fetching vehicle entity by id: {}", id);
        return vehicleRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new AppException("Vehicle not found with id: " + id, 404));
    }

    @Override
    public Optional<Vehicle> getVehicleEntityByLicensePlate(String licensePlate) {
        log.debug("Fetching vehicle entity by license plate: {}", licensePlate);
        return vehicleRepository.findByLicensePlate(licensePlate);
    }
    
    // ================================================================
    // DTO METHODS - For REST API responses
    // ================================================================
    
    @Override
    public List<VehicleResponseDTO> getAllVehicles() {
        // Get entities without category (no JOIN FETCH)
        List<Vehicle> vehicles = this.getAllVehiclesEntity();
        
        // Map to DTOs (only category ID)
        return vehicleMapper.toDtoList(vehicles);
    }
    
    @Override
    public List<VehicleDetailDTO> getAllVehiclesWithCategory() {
        // Get entities with category (LEFT JOIN FETCH)
        List<Vehicle> vehicles = vehicleRepository.findAllWithCategory();
        
        // Map to Detail DTOs (full category)
        return vehicles.stream()
            .map(vehicleMapper::toDetailDto)
            .toList();
    }

    @Override
    public List<VehicleDetailDTO> getVehiclesByCategory(Long categoryId) {
        log.debug("Fetching vehicles by category id: {}", categoryId);
        
        // Verify category exists
        categoryService.getCategoryEntityById(categoryId);
        
        // Get vehicles with category (JOIN FETCH)
        List<Vehicle> vehicles = vehicleRepository.findByCategoryIdWithCategory(categoryId);
        log.debug("Found {} vehicles in category {}", vehicles.size(), categoryId);
        
        // Map to Detail DTOs (full category)
        List<VehicleDetailDTO> dtos = vehicles.stream()
            .map(vehicleMapper::toDetailDto)
            .toList();
        
        return dtos;
    }
    
    @Override
    public VehicleDetailDTO getVehicleById(Long id) {
        // Get entity with category (JOIN FETCH)
        Vehicle vehicle = this.getVehicleEntityWithCategoryById(id);

        // Map to Detail DTO (full category)
        return vehicleMapper.toDetailDto(vehicle);
    }
    
    @Override
    public VehicleDetailDTO getVehicleByLicensePlate(String licensePlate) {
        log.debug("Fetching vehicle by license plate: {}", licensePlate);
        
        // Get vehicle by license plate (without category)
        Vehicle vehicleWithCategory = vehicleRepository.findByLicensePlateWithCategory(licensePlate)
            .orElseThrow(() -> new AppException(
                "Vehicle not found with license plate: " + licensePlate, 
                404)
            );
        
        return vehicleMapper.toDetailDto(vehicleWithCategory);
    }

    @Override
    public List<VehicleResponseDTO> findAvailableVehicles(Double requiredWeightKg, Double requiredVolumeCbm) {
        log.debug("Finding available vehicles for weight: {}kg, volume: {}m³", requiredWeightKg, requiredVolumeCbm);
        
        // Get all available vehicles
        List<Vehicle> availableVehicles = vehicleRepository.findByStatus(VehicleStatus.AVAILABLE);
        
        // Filter by capacity requirements
        List<Vehicle> matchingVehicles = availableVehicles.stream()
            .filter(vehicle -> meetsRequirements(vehicle, requiredWeightKg, requiredVolumeCbm))
            .collect(Collectors.toList());
        
        log.info("Found {} vehicles matching requirements out of {} available", 
            matchingVehicles.size(), availableVehicles.size());
        
        // Map to DTOs (only category ID)
        return vehicleMapper.toDtoList(matchingVehicles);
    }

    /**
     * Checks if a vehicle meets the weight and volume requirements.
     * 
     * @param vehicle - The vehicle to check
     * @param requiredWeightKg - Required weight capacity
     * @param requiredVolumeCbm - Required volume capacity
     * @return true if vehicle meets requirements, false otherwise
     */
    private boolean meetsRequirements(Vehicle vehicle, Double requiredWeightKg, Double requiredVolumeCbm) {
        // Check weight capacity
        boolean meetsWeight = vehicle.getMaxWeightKg() == null || 
                              vehicle.getMaxWeightKg() >= requiredWeightKg;
        
        // Check volume capacity
        boolean meetsVolume = vehicle.getMaxVolumeCbm() == null || 
                              vehicle.getMaxVolumeCbm() >= requiredVolumeCbm;
        
        return meetsWeight && meetsVolume;
    }
    
    // ================================================================
    // CRUD OPERATIONS - Write operations (Transactional)
    // ================================================================
    
    @Override
    @Transactional
    public VehicleDetailDTO createVehicle(VehicleRequestDTO request) {
        log.info("Creating new vehicle with license plate: {}", request.getLicensePlate());
        
        // Step 1: Validate business rules
        validateUniqueLicensePlate(request.getLicensePlate(), null);
        
        // Step 2: Map DTO to Entity
        Vehicle vehicle = vehicleMapper.toEntity(request);
        log.debug("Mapped request to entity - plate: {}, year: {}", 
            vehicle.getLicensePlate(), vehicle.getYear());
        
        // Step 3: Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryEntityById(request.getCategoryId());
            vehicle.setCategory(category);
            log.debug("Set category: {}", category.getName());
        }
        
        // STEP 4: Explicit .save() IS REQUIRED here for creation.
        // Unlike the update process, this transient object does not exist yet in the 
        // Hibernate persistence context (there is no initial snapshot to compare against).
        // Calling .save() forces Hibernate to execute an INSERT statement and, most importantly,
        // retrieves the database-generated auto-incremental primary key (ID).
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with id: {}", saved.getId());
        
        // Step 5: Map directly to DTO (no need to fetch again from DB)
        // The 'saved' managed entity in memory already contains the complete category object.
        return vehicleMapper.toDetailDto(saved);
    }
    
    @Override
    @Transactional
    public VehicleDetailDTO updateVehicle(Long id, VehicleRequestDTO request) {
        log.info("Updating vehicle with id: {}", id);
        
        // Step 1: Check exists
        // It does not matter that we fetch a LAZY relationship here because calling 
        // getCategory().getId() will check the foreign key already present in the vehicle proxy.
        // If we were to call getCategory().getName(), it would trigger an extra query (lazy loading initialization).
        Vehicle v = this.getVehicleEntityById(id);
        
        // Step 2: Validate business rules
        if (!v.getLicensePlate().equals(request.getLicensePlate())) {
            validateUniqueLicensePlate(request.getLicensePlate(), id);
        }
        
        // Step 3: Update entity fields
        v.setLicensePlate(request.getLicensePlate());
        v.setYear(request.getYear());
        
        // Step 4: Update category if provided
        if (request.getCategoryId() != null) {
            // Check if it is the same category to avoid an unnecessary database select.
            // v.getCategory().getId() does not trigger another query because Hibernate resolves this
            // using the foreign key value already stored within the Vehicle proxy object.
            Long currentId = (v.getCategory() != null) ? v.getCategory().getId() : null;
            if (!request.getCategoryId().equals(currentId)) {
                Category c = categoryService.getCategoryEntityById(request.getCategoryId());
                v.setCategory(c);
                log.debug("Updated category to: {}", c.getName());
            }
        } else {
            v.setCategory(null);
            log.debug("Removed category");
        }
        
        // STEP 5 OMITTED: Manual .save() is not required thanks to @Transactional.
        // This is because the transaction mechanism automatically saves changes by comparing two snapshots:
        // the initial state captured when the vehicle was retrieved, and the modified state up to this point.
        // Spring/Hibernate then performs an automatic flush, meaning an implicit save/update is executed.
        
        // The managed entity currently in memory is ready to be mapped and returned as the response.
        VehicleDetailDTO response = vehicleMapper.toDetailDto(v);
        log.debug("Mapped updated entity to response DTO");
        
        return response;
    }
    
    @Override
    @Transactional
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with id: {}", id);
        
        // Step 1: Check exists
        Vehicle vehicle = getVehicleEntityById(id);
        log.debug("Found vehicle to delete - plate: {}", vehicle.getLicensePlate());
        
        // Step 2: Delete
        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully - id: {}, plate: {}", 
            id, vehicle.getLicensePlate());
    }
    
    // ================================================================
    // PRIVATE HELPER METHODS
    // ================================================================
    
    /**
     * Validates that a license plate is unique.
     * 
     * @param licensePlate - The license plate to validate
     * @param excludeId - ID to exclude from check (null for create)
     * @throws AppException if license plate already exists
     */
    private void validateUniqueLicensePlate(String licensePlate, Long excludeId) {
        log.debug("Validating unique license plate: {}, excludeId: {}", licensePlate, excludeId);
        
        // Check if license plate exists
        Optional<Vehicle> existing = vehicleRepository.findByLicensePlate(licensePlate);
        
        // If no existing vehicle → License plate is available
        if (existing.isEmpty()) {
            log.debug("License plate '{}' is available", licensePlate);
            return;
        }
        
        Vehicle vehicle = existing.get();
        
        // Case 1: Creation (excludeId == null) → Always conflict
        if (excludeId == null) {
            log.warn("Duplicate license plate rejected during creation: {}", licensePlate);
            throw new AppException("License plate already exists: " + licensePlate, 409);
        }
        
        // Case 2: Update - Only conflict if it's a DIFFERENT vehicle
        if (!vehicle.getId().equals(excludeId)) {
            log.warn("License plate conflict - plate '{}' already exists on vehicle id: {}", 
                licensePlate, vehicle.getId());
            throw new AppException(
                String.format(
                    "License plate '%s' is already used by another vehicle (id: %d)", 
                    licensePlate, vehicle.getId()
                ), 
                409
            );
        }
        
        // Case 3: Same vehicle → OK, no conflict
        log.debug("License plate '{}' belongs to the same vehicle (id: {}), no conflict", 
            licensePlate, vehicle.getId());
    }
}