package com.fleets;

import com.fleets.config.DataSeeder;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
    public CommandLineRunner initData(DataSeeder dataSeeder) {
        return args -> {
            dataSeeder.seedData();
        };
    }
} 