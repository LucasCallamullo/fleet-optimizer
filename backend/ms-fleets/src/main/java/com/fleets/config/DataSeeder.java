// src/main/java/com/fleets/config/DataSeeder.java
package com.fleets.config;

import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.model.VehicleStatus;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

/**
 * DataSeeder - Initial data loader for the application.
 * 
 * This class seeds the database with initial categories and vehicles
 * for development and testing purposes.
 * 
 * @Transactional ensures all operations are atomic and roll back on failure
 */
@Component
public class DataSeeder {

    private final CategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;

    public DataSeeder(CategoryRepository categoryRepository, VehicleRepository vehicleRepository) {
        this.categoryRepository = categoryRepository;
        this.vehicleRepository = vehicleRepository;
    }

    /**
     * Main seeding method.
     * Checks if data already exists before seeding to avoid duplicates.
     */
    @Transactional
    public void seedData() {
        // Check if data already exists
        if (categoryRepository.count() > 0 && vehicleRepository.count() > 0) {
            System.out.println("📦 Data already exists - skipping seed");
            return;
        }

        System.out.println("🌱 Seeding initial data...");

        try {
            // Step 1: Create categories
            List<Category> categories = createCategories();

            // Step 2: Create vehicles with all fields
            createVehicles(categories);

            // Step 3: Summary
            System.out.println("✅ Seeded " + categoryRepository.count() + " categories");
            System.out.println("✅ Seeded " + vehicleRepository.count() + " vehicles");
            System.out.println("📊 Seed completed successfully!");
        } catch (Exception e) {
            System.err.println("❌ Error seeding data: " + e.getMessage());
            throw e; // Re-throw to trigger rollback
        }
    }

    /**
     * Creates and saves all vehicle categories.
     */
    private List<Category> createCategories() {
        Category truck = new Category();
        truck.setName("Truck");
        categoryRepository.save(truck);

        Category van = new Category();
        van.setName("Van");
        categoryRepository.save(van);

        Category car = new Category();
        car.setName("Car");
        categoryRepository.save(car);

        Category motorcycle = new Category();
        motorcycle.setName("Motorcycle");
        categoryRepository.save(motorcycle);

        Category suv = new Category();
        suv.setName("SUV");
        categoryRepository.save(suv);

        Category bus = new Category();
        bus.setName("Bus");
        categoryRepository.save(bus);

        return Arrays.asList(truck, van, car, motorcycle, suv, bus);
    }

    /**
     * Creates and saves vehicles with realistic data.
     * Each vehicle includes:
     * - License plate (unique)
     * - Year
     * - Category reference
     * - Physical capacities (weight, volume)
     * - Efficiency metrics (fuel consumption)
     * - Financial data (cost, price per km)
     * - Status (AVAILABLE, IN_ROUTE, MAINTENANCE, INACTIVE)
     */
    private void createVehicles(List<Category> categories) {
        // Extract categories for easy reference
        Category truck = categories.get(0);
        Category van = categories.get(1);
        Category car = categories.get(2);
        Category motorcycle = categories.get(3);
        Category suv = categories.get(4);
        Category bus = categories.get(5);

        // ================================================================
        // 2.1 TRUCKS - Heavy cargo vehicles
        // ================================================================
        Vehicle truck1 = new Vehicle();
        truck1.setLicensePlate("ABC123");
        truck1.setYear(2020);
        truck1.setCategory(truck);
        truck1.setMaxWeightKg(12000.0);
        truck1.setMaxVolumeCbm(45.0);
        truck1.setFuelConsumptionPerKm(0.35);
        truck1.setCostPerKm(2.50);
        truck1.setPricePerKm(4.80);
        truck1.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(truck1);

        Vehicle truck2 = new Vehicle();
        truck2.setLicensePlate("XYZ789");
        truck2.setYear(2021);
        truck2.setCategory(truck);
        truck2.setMaxWeightKg(15000.0);
        truck2.setMaxVolumeCbm(55.0);
        truck2.setFuelConsumptionPerKm(0.40);
        truck2.setCostPerKm(3.00);
        truck2.setPricePerKm(5.50);
        truck2.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(truck2);

        Vehicle truck3 = new Vehicle();
        truck3.setLicensePlate("TRK456");
        truck3.setYear(2022);
        truck3.setCategory(truck);
        truck3.setMaxWeightKg(8000.0);
        truck3.setMaxVolumeCbm(30.0);
        truck3.setFuelConsumptionPerKm(0.28);
        truck3.setCostPerKm(2.20);
        truck3.setPricePerKm(4.20);
        truck3.setStatus(VehicleStatus.IN_TRANSIT);
        vehicleRepository.save(truck3);

        // ================================================================
        // 2.2 VANS - Medium cargo vehicles
        // ================================================================
        Vehicle van1 = new Vehicle();
        van1.setLicensePlate("VAN001");
        van1.setYear(2021);
        van1.setCategory(van);
        van1.setMaxWeightKg(3500.0);
        van1.setMaxVolumeCbm(18.0);
        van1.setFuelConsumptionPerKm(0.15);
        van1.setCostPerKm(1.80);
        van1.setPricePerKm(3.20);
        van1.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(van1);

        Vehicle van2 = new Vehicle();
        van2.setLicensePlate("VAN002");
        van2.setYear(2022);
        van2.setCategory(van);
        van2.setMaxWeightKg(4000.0);
        van2.setMaxVolumeCbm(22.0);
        van2.setFuelConsumptionPerKm(0.18);
        van2.setCostPerKm(2.00);
        van2.setPricePerKm(3.50);
        van2.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(van2);

        // ================================================================
        // 2.3 CARS - Passenger vehicles
        // ================================================================
        Vehicle car1 = new Vehicle();
        car1.setLicensePlate("CAR456");
        car1.setYear(2022);
        car1.setCategory(car);
        car1.setMaxWeightKg(500.0);
        car1.setMaxVolumeCbm(2.5);
        car1.setFuelConsumptionPerKm(0.08);
        car1.setCostPerKm(1.20);
        car1.setPricePerKm(2.50);
        car1.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(car1);

        Vehicle car2 = new Vehicle();
        car2.setLicensePlate("CAR789");
        car2.setYear(2023);
        car2.setCategory(car);
        car2.setMaxWeightKg(600.0);
        car2.setMaxVolumeCbm(3.0);
        car2.setFuelConsumptionPerKm(0.09);
        car2.setCostPerKm(1.30);
        car2.setPricePerKm(2.80);
        car2.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(car2);

        Vehicle car3 = new Vehicle();
        car3.setLicensePlate("CAR111");
        car3.setYear(2020);
        car3.setCategory(car);
        car3.setMaxWeightKg(450.0);
        car3.setMaxVolumeCbm(2.0);
        car3.setFuelConsumptionPerKm(0.07);
        car3.setCostPerKm(1.10);
        car3.setPricePerKm(2.20);
        car3.setStatus(VehicleStatus.OUT_OF_SERVICE);
        vehicleRepository.save(car3);

        // ================================================================
        // 2.4 MOTORCYCLES - Small delivery vehicles
        // ================================================================
        Vehicle motorcycle1 = new Vehicle();
        motorcycle1.setLicensePlate("MOTO001");
        motorcycle1.setYear(2021);
        motorcycle1.setCategory(motorcycle);
        motorcycle1.setMaxWeightKg(150.0);
        motorcycle1.setMaxVolumeCbm(0.8);
        motorcycle1.setFuelConsumptionPerKm(0.04);
        motorcycle1.setCostPerKm(0.80);
        motorcycle1.setPricePerKm(1.50);
        motorcycle1.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(motorcycle1);

        Vehicle motorcycle2 = new Vehicle();
        motorcycle2.setLicensePlate("MOTO002");
        motorcycle2.setYear(2022);
        motorcycle2.setCategory(motorcycle);
        motorcycle2.setMaxWeightKg(200.0);
        motorcycle2.setMaxVolumeCbm(1.0);
        motorcycle2.setFuelConsumptionPerKm(0.05);
        motorcycle2.setCostPerKm(0.90);
        motorcycle2.setPricePerKm(1.80);
        motorcycle2.setStatus(VehicleStatus.IN_TRANSIT);
        vehicleRepository.save(motorcycle2);

        // ================================================================
        // 2.5 SUVs - Versatile vehicles
        // ================================================================
        Vehicle suv1 = new Vehicle();
        suv1.setLicensePlate("SUV001");
        suv1.setYear(2023);
        suv1.setCategory(suv);
        suv1.setMaxWeightKg(800.0);
        suv1.setMaxVolumeCbm(4.0);
        suv1.setFuelConsumptionPerKm(0.12);
        suv1.setCostPerKm(1.60);
        suv1.setPricePerKm(3.00);
        suv1.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(suv1);

        // ================================================================
        // 2.6 BUSES - Large passenger vehicles
        // ================================================================
        Vehicle bus1 = new Vehicle();
        bus1.setLicensePlate("BUS001");
        bus1.setYear(2020);
        bus1.setCategory(bus);
        bus1.setMaxWeightKg(18000.0);
        bus1.setMaxVolumeCbm(60.0);
        bus1.setFuelConsumptionPerKm(0.50);
        bus1.setCostPerKm(4.00);
        bus1.setPricePerKm(7.00);
        bus1.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(bus1);

        Vehicle bus2 = new Vehicle();
        bus2.setLicensePlate("BUS002");
        bus2.setYear(2021);
        bus2.setCategory(bus);
        bus2.setMaxWeightKg(20000.0);
        bus2.setMaxVolumeCbm(70.0);
        bus2.setFuelConsumptionPerKm(0.55);
        bus2.setCostPerKm(4.50);
        bus2.setPricePerKm(7.50);
        bus2.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(bus2);
    }
}