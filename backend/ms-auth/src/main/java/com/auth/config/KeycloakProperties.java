package com.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for Keycloak integration.
 * 
 * This class binds properties from application.yml under the prefix "app.keycloak".
 * 
 * <p><strong>Property Mapping:</strong>
 * <pre>
 * application.yml:
 *   app:
 *     keycloak:
 *       uri: https://labsys.frc.utn.edu.ar/aim
 *       realm: dds-materia
 *       client-id: some-client
 *       client-secret: some-secret
 * 
 * Java fields:
 *   uri         ← maps to app.keycloak.uri
 *   realm       ← maps to app.keycloak.realm
 *   clientId    ← maps to app.keycloak.client-id (hyphen to camelCase)
 *   clientSecret← maps to app.keycloak.client-secret
 * </pre>
 * 
 * <p><strong>Naming Convention:</strong>
 * Spring Boot automatically converts kebab-case (client-id) to camelCase (clientId).
 * This is standard behavior for @ConfigurationProperties.
 * 
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.keycloak")
public class KeycloakProperties {
    
    /**
     * Keycloak server base URL (e.g., https://labsys.frc.utn.edu.ar/aim)
     * Maps from: app.keycloak.uri
     */
    private String uri;
    
    /**
     * Keycloak realm name (e.g., dds-materia)
     * Maps from: app.keycloak.realm
     */
    private String realm;
    
    /**
     * Client ID for the application (e.g., fleet-client)
     * Maps from: app.keycloak.client-id (kebab-case → camelCase)
     */
    private String clientId;
    
    /**
     * Client secret for authentication
     * Maps from: app.keycloak.client-secret
     * Should be stored securely (e.g., environment variables)
     */
    private String clientSecret;
    
    /**
     * Admin username for Keycloak management operations
     * Maps from: app.keycloak.admin-user
     * Used for creating users and assigning roles
     */
    private String adminUser;
    
    /**
     * Admin password for Keycloak management operations
     * Maps from: app.keycloak.admin-password
     * Should be stored securely (e.g., environment variables)
     */
    private String adminPassword;
}