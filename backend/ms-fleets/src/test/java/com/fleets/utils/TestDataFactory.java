package com.fleets.utils;

import com.fleets.dto.request.VehicleRequestDTO;
import com.fleets.model.Category;
import com.fleets.model.Vehicle;

/**
 * Factory class for creating test data.
 * Similar to pytest fixtures - provides reusable test data.
 * 
 * Each method returns a NEW instance, ensuring test isolation.
 */
public class TestDataFactory {

    // ================================================================
    // CATEGORY FACTORIES
    // ================================================================
    
    public static Category createCategory(Long id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    public static Category createDefaultCategory() {
        return createCategory(1L, "Sedan");
    }

    public static Category createCategorySUV() {
        return createCategory(2L, "SUV");
    }

    public static Category createCategoryTruck() {
        return createCategory(3L, "Truck");
    }

    // ================================================================
    // VEHICLE FACTORIES
    // ================================================================
    
    public static Vehicle createVehicle(Long id, String licensePlate, Integer year, Category category) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setLicensePlate(licensePlate);
        vehicle.setYear(year);
        vehicle.setCategory(category);
        return vehicle;
    }

    public static Vehicle createDefaultVehicle() {
        return createVehicle(1L, "ABC123", 2023, createDefaultCategory());
    }

    public static Vehicle createVehicleWithoutCategory() {
        return createVehicle(2L, "XYZ789", 2024, null);
    }

    public static Vehicle createVehicleWithSUVCategory() {
        return createVehicle(3L, "DEF456", 2025, createCategorySUV());
    }

    // ================================================================
    // VEHICLE REQUEST DTO FACTORIES
    // ================================================================
    
    public static VehicleRequestDTO createVehicleRequestDTO(String licensePlate, Integer year, Long categoryId) {
        VehicleRequestDTO dto = new VehicleRequestDTO();
        dto.setLicensePlate(licensePlate);
        dto.setYear(year);
        dto.setCategoryId(categoryId);
        return dto;
    }

    public static VehicleRequestDTO createDefaultVehicleRequestDTO() {
        return createVehicleRequestDTO("ABC123", 2023, 1L);
    }

    public static VehicleRequestDTO createVehicleRequestDTOWithoutCategory() {
        return createVehicleRequestDTO("XYZ789", 2024, null);
    }

    public static VehicleRequestDTO createVehicleRequestDTOInvalidPlate() {
        return createVehicleRequestDTO("A", 2023, 1L);  // Too short
    }

    public static VehicleRequestDTO createVehicleRequestDTOInvalidYear() {
        return createVehicleRequestDTO("ABC123", 1800, 1L);  // Too old
    }
}