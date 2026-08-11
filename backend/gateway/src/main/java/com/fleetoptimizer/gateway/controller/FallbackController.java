package com.fleetoptimizer.gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/auth")
    public Mono<Map<String, Object>> authFallback() {
        return Mono.just(Map.of(
            "status", 503,
            "error", "Authentication service is currently unavailable. Please try again later.",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/fleets")
    public Mono<Map<String, Object>> fleetsFallback() {
        return Mono.just(Map.of(
            "status", 503,
            "error", "Fleet service is currently unavailable. Please try again later.",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/routes")
    public Mono<Map<String, Object>> routesFallback() {
        return Mono.just(Map.of(
            "status", 503,
            "error", "Routes service is currently unavailable. Please try again later.",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/packages")
    public Mono<Map<String, Object>> packagesFallback() {
        return Mono.just(Map.of(
            "status", 503,
            "error", "Packages service is currently unavailable. Please try again later.",
            "timestamp", System.currentTimeMillis()
        ));
    }

    @GetMapping("/geocoding")
    public Mono<Map<String, Object>> geocodingFallback() {
        return Mono.just(Map.of(
            "status", 503,
            "error", "Geocoding service is currently unavailable. Please try again later.",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
