package com.geocoding;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
public class MsGeocodingApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsGeocodingApplication.class, args);
    }

    /**
     * WebClient bean for calling OSRM API.
     * This is a lightweight HTTP client for making reactive calls.
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
            .baseUrl("http://router.project-osrm.org")
            .build();
    }
}