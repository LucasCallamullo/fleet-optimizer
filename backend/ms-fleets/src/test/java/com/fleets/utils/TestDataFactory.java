package com.fleets.utils;

import java.util.ArrayList;
import java.util.List;

import com.fleets.model.Category;
import com.fleets.model.Vehicle;

/**
 * Factory class for creating test data.
 * Similar to pytest fixtures - provides reusable test data.
 * 
 * Each method returns a NEW instance, ensuring test isolation.
 */
public class TestDataFactory {

    /**
     * Creates a complete dataset for integration tests.
     * Returns a list of vehicles ready to be saved to the database.
     * This is useful for @BeforeEach setup methods.
     */
    public static List<Vehicle> createSeedVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        
        // Crear categorías primero
        Category sedan = createDefaultCategory();
        Category suv = createCategorySUV();
        Category truck = createCategoryTruck();
        
        // Crear vehículos con diferentes categorías
        vehicles.add(createVehicle("ABC123", 2023, sedan));
        vehicles.add(createVehicle("XYZ789", 2024, suv));
        vehicles.add(createVehicle("DEF456", 2025, truck));
        vehicles.add(createVehicle("GHI789", 2022, sedan));
        vehicles.add(createVehicle("JKL012", 2023, suv));
        
        return vehicles;
    }

    /**
     * Creates a minimal dataset (just 2 vehicles) for simple tests.
     */
    public static List<Vehicle> createMinimalSeedVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(createDefaultVehicle());
        vehicles.add(createVehicleWithSUVCategory());
        return vehicles;
    }

    // ================================================================
    // CATEGORY FACTORIES
    // ================================================================
    
    public static Category createCategory(String name) {
        Category category = new Category();
        category.setName(name);
        return category;
    }

    public static Category createDefaultCategory() {
        return createCategory("Sedan");
    }

    public static Category createCategorySUV() {
        return createCategory("SUV");
    }

    public static Category createCategoryTruck() {
        return createCategory("Truck");
    }

    // ================================================================
    // VEHICLE FACTORIES
    // ================================================================
    
    public static Vehicle createVehicle(String licensePlate, Integer year, Category category) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setYear(year);
        vehicle.setCategory(category);
        return vehicle;
    }

    public static Vehicle createDefaultVehicle() {
        return createVehicle("ABC123", 2023, createDefaultCategory());
    }

    public static Vehicle createVehicleWithoutCategory() {
        return createVehicle("XYZ789", 2024, null);
    }

    public static Vehicle createVehicleWithSUVCategory() {
        return createVehicle("DEF456", 2025, createCategorySUV());
    }
}