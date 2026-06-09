package com.fleets.service.impl;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.exception.AppException;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.VehicleRepository;
import com.fleets.service.CategoryService;
import com.fleets.service.VehicleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * JPA-based implementation of VehicleService.
 * Uses Spring Data JPA for database operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    
    private final VehicleRepository vehicleRepository;
    private final CategoryService categoryService;
    
    @Override
    public List<Vehicle> getAllVehicles() {
        log.debug("Fetching all vehicles from database");
        List<Vehicle> vehicles = vehicleRepository.findAll();
        log.debug("Found {} vehicles in database", vehicles.size());
        return vehicles;
    }
    
    @Override
    public Vehicle getVehicleById(Long id) {
        log.debug("Fetching vehicle by id: {}", id);
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new AppException("Vehicle not found with id: " + id, 404));
    }
    
    @Override
    public Vehicle createVehicle(VehicleRequestDTO request) {
        log.info("Creating new vehicle with license plate: {}", request.getLicensePlate());
        
        // Check for duplicate license plate
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            log.warn("Duplicate license plate rejected: {}", request.getLicensePlate());
            throw new AppException("License plate already exists: " + request.getLicensePlate(), 409);
        }
        
        // Convert DTO to Entity
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setYear(request.getYear());
        
        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId());
            vehicle.setCategory(category);
        }
        
        Vehicle saved = vehicleRepository.save(vehicle);
        log.info("Vehicle created successfully with id: {}", saved.getId());
        return saved;
    }
    
    @Override
    public Vehicle updateVehicle(Long id, VehicleRequestDTO request) {
        log.info("Updating vehicle with id: {}", id);
        log.debug("Update details - plate: {}, year: {}", request.getLicensePlate(), request.getYear());
        
        Vehicle existingVehicle = getVehicleById(id);
        
        // Check if license plate is being changed and if it conflicts
        if (!existingVehicle.getLicensePlate().equals(request.getLicensePlate()) 
                && vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            log.warn("License plate conflict - plate {} already exists", request.getLicensePlate());
            throw new AppException("License plate already exists: " + request.getLicensePlate(), 409);
        }
        
        // Update fields
        existingVehicle.setLicensePlate(request.getLicensePlate());
        existingVehicle.setYear(request.getYear());
        
        // Update category if provided, otherwise set to null
        if (request.getCategoryId() != null) {
            Category category = categoryService.getCategoryById(request.getCategoryId());
            existingVehicle.setCategory(category);
        } else {
            existingVehicle.setCategory(null);
        }
        
        Vehicle updated = vehicleRepository.save(existingVehicle);
        log.info("Vehicle updated successfully - id: {}, plate: {}", updated.getId(), updated.getLicensePlate());
        return updated;
    }
    
    @Override
    public void deleteVehicle(Long id) {
        log.info("Deleting vehicle with id: {}", id);
        
        if (!vehicleRepository.existsById(id)) {
            throw new AppException("Vehicle not found with id: " + id, 404);
        }
        
        vehicleRepository.deleteById(id);
        log.info("Vehicle deleted successfully - id: {}", id);
    }
    
    @Override
    public List<Vehicle> getVehiclesByCategory(Long categoryId) {
        log.debug("Fetching vehicles by category id: {}", categoryId);
        List<Vehicle> vehicles = vehicleRepository.findByCategoryId(categoryId);
        log.debug("Found {} vehicles in category {}", vehicles.size(), categoryId);
        return vehicles;
    }
    
    @Override
    public Vehicle getVehicleByLicensePlate(String licensePlate) {
        log.debug("Fetching vehicle by license plate: {}", licensePlate);
        return vehicleRepository.findByLicensePlate(licensePlate);
    }
}