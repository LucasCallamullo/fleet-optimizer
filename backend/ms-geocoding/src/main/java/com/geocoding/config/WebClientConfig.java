package com.geocoding.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.ors.url}")
    private String orsUrl;

    @Bean
    public WebClient orsWebClient() {
        return WebClient.builder()
            .baseUrl(orsUrl)
            .build();
    }
}
