package com.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

/**
 * Configuration class for WebClient instances.
 * Centralizes non-blocking reactive HTTP clients for external Identity Providers.
 */
@Configuration
public class WebClientConfig {

    /**
     * WebClient instance configured for Keycloak Identity Provider.
     * 
     * Step-by-step:
     * 1. Takes KeycloakProperties (bound from application.yml)
     * 2. Extracts the base URL (properties.getUri())
     * 3. Builds a WebClient with that base URL
     * 4. This WebClient is now ready to make calls to Keycloak
     *
     * @param properties Keycloak properties (uri, realm, client-id, etc.)
     * @return WebClient configured with Keycloak base URI
     */
    @Bean
    public WebClient keycloakWebClient(KeycloakProperties properties) {
        return WebClient.builder()
            .baseUrl(properties.getUri())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    }

    /**
     * WebClient instance configured for Auth0 Identity Provider.
     *
     * @param properties Auth0 properties (domain, client-id, audience, etc.)
     * @return WebClient configured with Auth0 domain base URL
     */
    @Bean
    public WebClient auth0WebClient(Auth0Properties properties) {
        return WebClient.builder()
            .baseUrl(properties.getDomain())
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }
}
