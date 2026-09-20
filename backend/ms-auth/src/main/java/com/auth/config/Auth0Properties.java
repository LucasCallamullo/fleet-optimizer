package com.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties class for Auth0 authentication service.
 * Maps application properties defined under the prefix "app.auth0".
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.auth0")
public class Auth0Properties {

    /**
     * Auth0 tenant domain URL (e.g., https://dev-ejieic4zageec10c.us.auth0.com)
     * Maps from: app.auth0.domain
     */
    private String domain;

    /**
     * Client ID for Machine-to-Machine and authentication requests
     * Maps from: app.auth0.client-id (kebab-case -> camelCase)
     */
    private String clientId;

    /**
     * Client secret for Auth0 API authentication
     * Maps from: app.auth0.client-secret
     * Should be injected securely via environment variables
     */
    private String clientSecret;

    /**
     * Auth0 Management API Identifier/Audience (e.g., https://tenant.auth0.com/api/v2/)
     * Maps from: app.auth0.audience
     */
    private String audience;

    /**
     * This is only for login to app, is no related with token access for audience
     */
    private String loginAudience;

    /**
     * JWKS URI endpoint for public key fetching and JWT verification
     * Maps from: app.auth0.jwks-uri
     */
    private String jwksUri;
}
