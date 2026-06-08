package com.fleets.service.impl;

// import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.VehicleRepository;

// import com.fleets.service.CategoryService;
import com.fleets.service.VehicleService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * JPA-based implementation of VehicleService.
 * Uses Spring Data JPA for database operations.
 */
@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    
    private final VehicleRepository vehicleRepository;
    // private final CategoryService categoryService;
    
    @Override
    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
    
    @Override
    public Vehicle getVehicleById(Long id) {
        return vehicleRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Vehicle not found with id: " + id));
    }
    
    @Override
    public Vehicle createVehicle(Vehicle vehicle) {
        if (vehicleRepository.existsByLicensePlate(vehicle.getLicensePlate())) {
            throw new RuntimeException("License plate already exists: " + vehicle.getLicensePlate());
        }
        return vehicleRepository.save(vehicle);
    }
    
    @Override
    public Vehicle updateVehicle(Long id, Vehicle vehicleDetails) {
        Vehicle existingVehicle = getVehicleById(id);
        
        existingVehicle.setLicensePlate(vehicleDetails.getLicensePlate());
        existingVehicle.setYear(vehicleDetails.getYear());
        
        /* / Update category if provided
        if (vehicleDetails.getCategory() != null && vehicleDetails.getCategory().getId() != null) {
            Category category = categoryService.getCategoryById(vehicleDetails.getCategory().getId());
            existingVehicle.setCategory(category);
        } */
        
        return vehicleRepository.save(existingVehicle);
    }
    
    @Override
    public void deleteVehicle(Long id) {
        if (!vehicleRepository.existsById(id)) {
            throw new RuntimeException("Vehicle not found with id: " + id);
        }
        vehicleRepository.deleteById(id);
    }
    
    @Override
    public List<Vehicle> getVehiclesByCategory(Long categoryId) {
        return vehicleRepository.findByCategoryId(categoryId);
    }
    
    @Override
    public Vehicle getVehicleByLicensePlate(String licensePlate) {
        return vehicleRepository.findByLicensePlate(licensePlate);
    }
}