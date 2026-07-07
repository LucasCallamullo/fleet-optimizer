package com.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuration class for WebClient instances.
 * WebClient is a reactive HTTP client for making non-blocking HTTP requests.
 * 
 * In this case, it's used to communicate with Keycloak.
 */
@Configuration
public class WebClientConfig {

    /**
     * Creates a WebClient bean configured to call Keycloak.
     * 
     * Step-by-step:
     * 1. Takes KeycloakProperties (bound from application.yml)
     * 2. Extracts the base URL (properties.getUri())
     * 3. Builds a WebClient with that base URL
     * 4. This WebClient is now ready to make calls to Keycloak
     * 
     * Example: If uri = "https://labsys.frc.utn.edu.ar/aim"
     * then later calls can use relative paths:
     *   keycloakWebClient.post()
     *     .uri("/realms/{realm}/protocol/openid-connect/token")
     *   → Full URL: https://labsys.frc.utn.edu.ar/aim/realms/.../token
     * 
     * @param properties Keycloak configuration (uri, realm, client-id, etc.)
     * @return WebClient instance ready to use
     */
    @Bean
    public WebClient keycloakWebClient(KeycloakProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getUri())  // ← Extrae la URI base de Keycloak
            .build();                     // ← Construye el cliente HTTP
    }
}