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
    public static List<Vehicle> createSeedVehicles(List<Category> categories) {
        List<Vehicle> vehicles = new ArrayList<>();
        
        // Crear categorías primero
        Category sedanCategory = categories.get(0);
        Category suvCategory = categories.get(1);
        Category truckCategory = categories.get(2);
        
        // Crear vehículos con diferentes categorías
        vehicles.add(createVehicle("ABC123", 2023, sedanCategory));
        vehicles.add(createVehicle("XYZ789", 2024, suvCategory));
        vehicles.add(createVehicle("DEF456", 2025, truckCategory));
        vehicles.add(createVehicle("GHI789", 2022, sedanCategory));
        vehicles.add(createVehicle("JKL012", 2023, null));
        
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
}