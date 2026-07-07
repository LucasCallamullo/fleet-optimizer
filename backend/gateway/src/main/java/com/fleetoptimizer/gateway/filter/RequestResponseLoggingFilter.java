package com.fleetoptimizer.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Reactive Request and Response Logging Filter for Gateway.
 * 
 * Spring Cloud Gateway uses WebFlux (Netty), NOT Servlet (Tomcat).
 * Therefore, we use ServerWebExchange and GlobalFilter instead of
 * HttpServletRequest and OncePerRequestFilter.
 * 
 * Features:
 * - Colored console output for different HTTP methods and status codes
 * - Performance metrics (request duration in milliseconds)
 * - Client IP extraction from headers
 * - Excludes actuator, swagger, and api-docs endpoints
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter implements GlobalFilter, Ordered {

    // ANSI Color Codes for Console Output
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    private static final String ANSI_BOLD_RED = "\u001B[1;31m";
    private static final String ANSI_BOLD_GREEN = "\u001B[1;32m";
    private static final String ANSI_BOLD_YELLOW = "\u001B[1;33m";
    private static final String ANSI_BOLD_CYAN = "\u001B[1;36m";

    // HTTP Method Color Mapping
    private static final Map<String, String> METHOD_COLORS = Map.of(
        "GET", ANSI_GREEN,
        "POST", ANSI_PURPLE,
        "PUT", ANSI_YELLOW,
        "PATCH", ANSI_YELLOW,
        "DELETE", ANSI_RED,
        "OPTIONS", ANSI_BLUE,
        "HEAD", ANSI_WHITE
    );

    // Check if ANSI colors are supported
    private static final boolean COLOR_SUPPORTED = isColorSupported();

    private static boolean isColorSupported() {
        return true; // Most modern terminals support ANSI
    }

    private static String colorize(String text, String color) {
        return COLOR_SUPPORTED ? color + text + ANSI_RESET : text;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // Step 1: Capture request details
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        String method = request.getMethod() != null ? request.getMethod().name() : "UNKNOWN";
        String path = request.getURI().getPath();
        String query = request.getURI().getQuery();
        String fullUrl = query != null ? path + "?" + query : path;
        String clientIp = getClientIp(request);

        long startTime = System.currentTimeMillis();
        String methodColor = METHOD_COLORS.getOrDefault(method, ANSI_RESET);

        // Step 2: Check if this request should be excluded from logging
        if (shouldNotFilter(path)) {
            return chain.filter(exchange);
        }

        // Step 3: Execute the request and log response
        return chain.filter(exchange).then(
            Mono.fromRunnable(() -> {
                long duration = System.currentTimeMillis() - startTime;
                int status = response.getStatusCode() != null 
                    ? response.getStatusCode().value() 
                    : -1;

                String statusColor;
                // String statusLabel;
                if (status >= 500) {
                    statusColor = ANSI_BOLD_RED;
                    // statusLabel = "SERVER ERROR";
                } else if (status >= 400) {
                    statusColor = ANSI_BOLD_YELLOW;
                    // statusLabel = "CLIENT ERROR";
                } else if (status >= 300) {
                    statusColor = ANSI_BOLD_CYAN;
                    // statusLabel = "REDIRECT";
                } else if (status >= 200) {
                    statusColor = ANSI_BOLD_GREEN;
                    // statusLabel = "SUCCESS";
                } else {
                    statusColor = ANSI_RESET;
                    // statusLabel = "INFO";
                }

                String paddedMethod = String.format("%-7s", method);
                String coloredMethod = colorize(paddedMethod, methodColor);
                String coloredStatus = colorize(String.valueOf(status), statusColor);
                String coloredUrl = colorize(fullUrl, ANSI_WHITE);
                String coloredIp = colorize("[" + clientIp + "]", ANSI_BLUE);

                String durationColor = duration > 1000 ? ANSI_YELLOW 
                    : (duration > 500 ? ANSI_CYAN : ANSI_GREEN);
                String coloredDuration = colorize(duration + "ms", durationColor);

                if (status >= 500) {
                    log.error("{}{} {} - {} {} [SERVER ERROR]",
                        coloredMethod, coloredStatus, coloredUrl, coloredIp, coloredDuration);
                } else if (status >= 400) {
                    log.warn("{}{} {} - {} {} [CLIENT ERROR]",
                        coloredMethod, coloredStatus, coloredUrl, coloredIp, coloredDuration);
                } else {
                    log.info("{}{} {} - {} {}",
                        coloredMethod, coloredStatus, coloredUrl, coloredIp, coloredDuration);
                }
            })
        );
    }

    /**
     * Extracts client IP from request headers.
     */
    private String getClientIp(ServerHttpRequest request) {
        // Try to get from headers first
        String[] headers = {"X-Forwarded-For", "X-Real-IP", "Proxy-Client-IP", "WL-Proxy-Client-IP"};
        for (String header : headers) {
            String ip = request.getHeaders().getFirst(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                if (header.equals("X-Forwarded-For") && ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // Fallback to remote address
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        return remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
    }

    /**
     * Excludes certain paths from logging.
     */
    private boolean shouldNotFilter(String path) {
        return path.contains("/swagger-ui") ||
               path.contains("/api-docs") ||
               path.contains("/actuator") ||
               path.contains("/v3/api-docs");
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}