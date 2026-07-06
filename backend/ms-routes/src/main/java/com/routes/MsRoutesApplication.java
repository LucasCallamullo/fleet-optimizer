package com.routes;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
// import org.springframework.web.servlet.config.annotation.CorsRegistry;
// import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.routes.model.entity.Leg;
import com.routes.model.entity.Location;
import com.routes.model.entity.Route;
import com.routes.model.enums.LegStatus;
import com.routes.model.enums.RouteStatus;
import com.routes.repository.LegRepository;
import com.routes.repository.RouteRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableFeignClients            // feign clients need this
public class MsRoutesApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsRoutesApplication.class, args);
    }

    /**
	 * Seeds initial data into the database on application startup.
	 * Only runs when the database is empty.
	 */
	@Bean
    public CommandLineRunner initData(RouteRepository routeRepository, LegRepository legRepository) {
        return args -> {
            log.info("Seeding initial data...");

            // Check if data already exists
            if (routeRepository.count() > 0) {
                log.info("Data already seeded, skipping...");
                return;
            }

            // Step 1: Create a sample route
            Route route = new Route();
            route.setName("Buenos Aires - Cordoba - Mendoza");
            route.setDescription("Sample route for testing");
            route.setStatus(RouteStatus.PLANNED);
            route.setEstimatedDistanceKm(1350.0);
            route.setEstimatedDurationMinutes(900);

            Route savedRoute = routeRepository.save(route);
            log.info("Created sample route with id: {}", savedRoute.getId());

            // Step 2: Create sample legs
            Location origin1 = new Location();
            origin1.setStreet("Av. Libertador");
            origin1.setStreetNumber("1000");
            origin1.setCity("Buenos Aires");
            origin1.setState("CABA");
            origin1.setCountry("Argentina");
            origin1.setPostalCode("1000");
            origin1.setLatitude(-34.6037);
            origin1.setLongitude(-58.3816);

            Location dest1 = new Location();
            dest1.setStreet("Av. Colon");
            dest1.setStreetNumber("500");
            dest1.setCity("Cordoba");
            dest1.setState("Cordoba");
            dest1.setCountry("Argentina");
            dest1.setPostalCode("5000");
            dest1.setLatitude(-31.4201);
            dest1.setLongitude(-64.1888);

            Leg leg1 = new Leg();
            leg1.setSequence(1);
            leg1.setRoute(savedRoute);
            leg1.setStatus(LegStatus.PENDING);
            leg1.setOrigin(origin1);
            leg1.setDestination(dest1);
            leg1.setVehicleId(1L);
            leg1.setPackageId(1L);
            leg1.setDistanceKm(700.0);
            leg1.setDurationMinutes(480);

            legRepository.save(leg1);
            log.info("Created sample leg 1");

            Location origin2 = new Location();
            origin2.setStreet("Av. Colon");
            origin2.setStreetNumber("500");
            origin2.setCity("Cordoba");
            origin2.setState("Cordoba");
            origin2.setCountry("Argentina");
            origin2.setPostalCode("5000");
            origin2.setLatitude(-31.4201);
            origin2.setLongitude(-64.1888);

            Location dest2 = new Location();
            dest2.setStreet("Av. San Martin");
            dest2.setStreetNumber("100");
            dest2.setCity("Mendoza");
            dest2.setState("Mendoza");
            dest2.setCountry("Argentina");
            dest2.setPostalCode("5500");
            dest2.setLatitude(-32.8908);
            dest2.setLongitude(-68.8272);

            Leg leg2 = new Leg();
            leg2.setSequence(2);
            leg2.setRoute(savedRoute);
            leg2.setStatus(LegStatus.PENDING);
            leg2.setOrigin(origin2);
            leg2.setDestination(dest2);
            leg2.setVehicleId(2L);
            leg2.setPackageId(2L);
            leg2.setDistanceKm(650.0);
            leg2.setDurationMinutes(420);

            legRepository.save(leg2);
            log.info("Created sample leg 2");

            log.info("Seed data completed successfully!");
        };
    }

    /**
     * Configura CORS para permitir peticiones desde React (puerto 3000)
     * 
     * API Gatewaty se encarga de habilitar el cors externo
     
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/v1/**")        // ← Solo endpoints /api/*
                        .allowedOrigins("*")              // ← React "http://localhost:5173"
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        // .allowCredentials(true);             //
                        .allowCredentials(false);
            }
        };
    }
        */
} 