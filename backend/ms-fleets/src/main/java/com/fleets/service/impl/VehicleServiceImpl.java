package com.fleets.service.impl;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.dto.response.VehicleResponseDTO;
import com.fleets.dto.response.VehicleDetailDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
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
        List<Vehicle> vehicles = vehicleRepository.findAll();
        
        // Map to DTOs (only category ID)
        List<VehicleResponseDTO> dtos = vehicleMapper.toDtoList(vehicles);
        
        return dtos;
    }
    
    @Override
    public List<VehicleDetailDTO> getAllVehiclesWithCategory() {
        
        // Get entities with category (JOIN FETCH)
        List<Vehicle> vehicles = vehicleRepository.findAllWithCategory();
        
        // Map to Detail DTOs (full category)
        List<VehicleDetailDTO> dtos = vehicles.stream()
            .map(vehicleMapper::toDetailDto)
            .toList();
        
        return dtos;
    }
    
    @Override
    public VehicleDetailDTO getVehicleById(Long id) {
        
        // Get entity with category (JOIN FETCH)
        Vehicle vehicle = vehicleRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new AppException("Vehicle not found with id: " + id, 404));

        log.debug("Found vehicle - plate: {}, category: {}", 
            vehicle.getLicensePlate(), 
            vehicle.getCategory() != null ? vehicle.getCategory().getName() : "null");
        
        // Map to Detail DTO (full category)
        return vehicleMapper.toDetailDto(vehicle);
    }
    
    @Override
    public VehicleDetailDTO getVehicleByLicensePlate(String licensePlate) {
        log.debug("Fetching vehicle by license plate: {}", licensePlate);
        
        // Get vehicle by license plate (without category)
        Vehicle vehicleWithCategory = vehicleRepository.findByLicensePlateWithCategory(licensePlate)
            .orElseThrow(() -> new AppException("Vehicle not found with license plate: " + licensePlate, 404));
        
        return vehicleMapper.toDetailDto(vehicleWithCategory);
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
        
        // Step 4: Save entity
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with id: {}", saved.getId());
        
        // Step 5: Map Entity to DTO (for response)
        // We need to fetch with category for the response
        Vehicle savedWithCategory = vehicleRepository.findByIdWithCategory(saved.getId())
            .orElseThrow(() -> new AppException("Vehicle not found after creation", 500));
        
        VehicleDetailDTO response = vehicleMapper.toDetailDto(savedWithCategory);
        log.debug("Mapped saved entity to response DTO");
        
        return response;
    }
    
    @Override
    @Transactional
    public VehicleDetailDTO updateVehicle(Long id, VehicleRequestDTO request) {
        log.info("Updating vehicle with id: {}", id);
        log.debug("Update details - plate: {}, year: {}", 
            request.getLicensePlate(), request.getYear());
        
        // Step 1: Check exists
        Vehicle existingVehicle = this.getVehicleEntityById(id);
        log.debug("Found existing vehicle - plate: {}, year: {}", 
            existingVehicle.getLicensePlate(), existingVehicle.getYear());
        
        // Step 2: Validate business rules
        if (!existingVehicle.getLicensePlate().equals(request.getLicensePlate())) {
            validateUniqueLicensePlate(request.getLicensePlate(), id);
        }
        
        // Step 3: Update entity fields
        existingVehicle.setLicensePlate(request.getLicensePlate());
        existingVehicle.setYear(request.getYear());
        
        // Step 4: Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryEntityById(request.getCategoryId());
            existingVehicle.setCategory(category);
            log.debug("Updated category to: {}", category.getName());
        } else {
            existingVehicle.setCategory(null);
            log.debug("Removed category");
        }
        
        // Step 5: Save entity
        Vehicle updated = vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated successfully - id: {}, plate: {}", 
            updated.getId(), updated.getLicensePlate());
        
        // Step 6: Map Entity to DTO (for response)
        // We need to fetch with category for the response
        Vehicle updatedWithCategory = vehicleRepository.findByIdWithCategory(updated.getId())
            .orElseThrow(() -> new AppException("Vehicle not found after update", 500));
        
        VehicleDetailDTO response = vehicleMapper.toDetailDto(updatedWithCategory);
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
    // UTILITY METHODS
    // ================================================================
    
    @Override
    public boolean existsByLicensePlate(String licensePlate) {
        log.debug("Checking if vehicle exists by license plate: {}", licensePlate);
        boolean exists = vehicleRepository.existsByLicensePlate(licensePlate);
        log.debug("Vehicle exists: {} - {}", licensePlate, exists);
        return exists;
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
                String.format("License plate '%s' is already used by another vehicle (id: %d)", 
                    licensePlate, vehicle.getId()), 
                409
            );
        }
        
        // Case 3: Same vehicle → OK, no conflict
        log.debug("License plate '{}' belongs to the same vehicle (id: {}), no conflict", 
            licensePlate, vehicle.getId());
    }
}