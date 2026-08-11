package com.geocoding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient instances used in ms-geocoding.
 * 
 * This configuration creates a WebClient bean configured to communicate
 * with the OpenRouteService (ORS) API.
 * 
 * The base URL is injected from application.yml using the app.ors.url property,
 * which can be overridden by environment variables or Docker .env file.
 * 
 * WebClient is a reactive, non-blocking HTTP client from Spring WebFlux,
 * ideal for making asynchronous calls to external APIs like ORS.
 * 
 * Usage:
 * <pre>
 * &#64;Service
 * public class OrsService {
 *     private final WebClient orsWebClient;
 *     
 *     public Mono&lt;DistanceResponseDTO&gt; calculateDistance(...) {
 *         return orsWebClient.post()
 *             .uri("/v2/directions/driving-car/geojson")
 *             .header("Authorization", apiKey)
 *             .retrieve()
 *             .bodyToMono(JsonNode.class);
 *     }
 * }
 * </pre>
 * 
 * @see org.springframework.web.reactive.function.client.WebClient
 */
@Configuration
public class WebClientConfig {

    @Value("${app.ors.url}")
    private String orsUrl;

    /**
     * Creates a WebClient instance configured with the ORS base URL.
     * 
     * This WebClient is used for all HTTP calls to the OpenRouteService API.
     * It is injected into OrsService for making distance calculation requests.
     * 
     * @return WebClient with base URL set to app.ors.url
     */
    @Bean
    public WebClient orsWebClient() {
        return WebClient.builder()
            .baseUrl(orsUrl)
            .build();
    }
}