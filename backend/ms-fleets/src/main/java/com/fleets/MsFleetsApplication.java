package com.fleets;

import com.fleets.model.Category;
import com.fleets.model.Vehicle;
import com.fleets.repository.CategoryRepository;
import com.fleets.repository.VehicleRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class MsFleetsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsFleetsApplication.class, args);
	}

	/**
	 * Seeds initial data into the database on application startup.
	 * Only runs when the database is empty.
	 */
	@Bean
	public CommandLineRunner initData(CategoryRepository categoryRepository, VehicleRepository vehicleRepository) {
		return args -> {
			// Check if data already exists
			if (categoryRepository.count() > 0) {
				System.out.println("Data already exists - skipping seed");
				return;
			}

			System.out.println("Seeding initial data...");

			// Create categories
			Category truckCategory = new Category();
			truckCategory.setName("Truck");
			categoryRepository.save(truckCategory);

			Category carCategory = new Category();
			carCategory.setName("Car");
			categoryRepository.save(carCategory);

			Category motorcycleCategory = new Category();
			motorcycleCategory.setName("Motorcycle");
			categoryRepository.save(motorcycleCategory);

			// Create vehicles
			Vehicle truck1 = new Vehicle();
			truck1.setLicensePlate("ABC123");
			truck1.setYear(2020);
			truck1.setCategory(truckCategory);
			vehicleRepository.save(truck1);

			Vehicle truck2 = new Vehicle();
			truck2.setLicensePlate("XYZ789");
			truck2.setYear(2021);
			truck2.setCategory(truckCategory);
			vehicleRepository.save(truck2);

			Vehicle car1 = new Vehicle();
			car1.setLicensePlate("CAR456");
			car1.setYear(2022);
			car1.setCategory(carCategory);
			vehicleRepository.save(car1);

			Vehicle car2 = new Vehicle();
			car2.setLicensePlate("CAR789");
			car2.setYear(2023);
			car2.setCategory(carCategory);
			vehicleRepository.save(car2);

			Vehicle motorcycle1 = new Vehicle();
			motorcycle1.setLicensePlate("MOTO001");
			motorcycle1.setYear(2021);
			motorcycle1.setCategory(motorcycleCategory);
			vehicleRepository.save(motorcycle1);

			System.out.println("Seeded " + categoryRepository.count() + " categories");
			System.out.println("Seeded " + vehicleRepository.count() + " vehicles");
		};
	} 


	/**
     * Configura CORS para permitir peticiones desde React (puerto 3000)
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")           // ← Solo endpoints /api/*
                        .allowedOrigins("*")  // ← React "http://localhost:5173"
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // .allowCredentials(true); //
                        .allowCredentials(false);
            }
        };
    }
} 