package com.packages;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.packages.config.SeedDataService;
import lombok.RequiredArgsConstructor;

@SpringBootApplication
@RequiredArgsConstructor
public class MsPackagesApplication {

	private final SeedDataService seedDataService;

	public static void main(String[] args) {
		SpringApplication.run(MsPackagesApplication.class, args);
	}

	@Bean
    public CommandLineRunner initData() {
        return args -> {
            seedDataService.seedAllData();
        };
    }
}