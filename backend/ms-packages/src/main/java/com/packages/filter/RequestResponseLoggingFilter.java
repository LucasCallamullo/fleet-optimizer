package com.packages.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Request and Response Logging Filter
 * 
 * This filter intercepts all HTTP requests and logs detailed information
 * including method, URL, status code, and execution time.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    // ANSI Color Codes for Console Output
    private static final String ANSI_RESET = "\u001B[0m";
    // private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_PURPLE = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";
    
    // Bold variants for better visibility
    // private static final String ANSI_BOLD = "\u001B[1m";
    private static final String ANSI_BOLD_RED = "\u001B[1;31m";
    private static final String ANSI_BOLD_GREEN = "\u001B[1;32m";
    private static final String ANSI_BOLD_YELLOW = "\u001B[1;33m";
    private static final String ANSI_BOLD_CYAN = "\u001B[1;36m";

    // HTTP Method Color Mapping
    private static final Map<String, String> METHOD_COLORS = Map.of(
            "GET", ANSI_GREEN,
            "POST", ANSI_PURPLE,    // ANSI_CYAN
            "PUT", ANSI_YELLOW,
            "PATCH", ANSI_YELLOW,
            "DELETE", ANSI_RED,
            "OPTIONS", ANSI_BLUE,
            "HEAD", ANSI_WHITE
    );

    // Check if ANSI colors are supported (terminal or IDE)
    private static final boolean COLOR_SUPPORTED = isColorSupported();

    /**
     * Check if the current console supports ANSI colors
     * 
     * @return true if ANSI colors are supported, false otherwise
     */
    private static boolean isColorSupported() {
        // Check if running in an IDE or terminal that supports colors
        // String os = System.getProperty("os.name").toLowerCase();
        // boolean isWindows = os.contains("win");
        
        // Windows CMD doesn't support ANSI by default, but Windows Terminal does
        /* if (isWindows) {
            String term = System.getenv("TERM");
            String wt = System.getenv("WT_SESSION");
            return term != null || wt != null;
        } */
        return true; // Linux, macOS, and modern terminals support ANSI
    }

    /**
     * Apply color to a string if ANSI colors are supported
     * 
     * @param text The text to color
     * @param color The ANSI color code
     * @return Colored text if supported, plain text otherwise
     */
    private static String colorize(String text, String color) {
        if (COLOR_SUPPORTED) {
            return color + text + ANSI_RESET;
        }
        return text;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Capture start time, method, and URL
        long startTime = System.currentTimeMillis();
        String method = request.getMethod();
        String url = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? url + "?" + queryString : url;
        String clientIp = getClientIp(request);

        // Get color for the HTTP method
        String methodColor = METHOD_COLORS.getOrDefault(method, ANSI_RESET);

        try {
            // 2. Proceed with the request chain (execute controller)
            filterChain.doFilter(request, response);
        } finally {
            // 3. Log after response is complete
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            // Determine status color
            String statusColor;
            String statusLabel;
            if (status >= 500) {
                statusColor = ANSI_BOLD_RED;
                statusLabel = "ERROR";
            } else if (status >= 400) {
                statusColor = ANSI_BOLD_YELLOW;
                statusLabel = "CLIENT ERROR";
            } else if (status >= 300) {
                statusColor = ANSI_BOLD_CYAN;
                statusLabel = "REDIRECT";
            } else if (status >= 200) {
                statusColor = ANSI_BOLD_GREEN;
                statusLabel = "SUCCESS";
            } else {
                statusColor = ANSI_RESET;
                statusLabel = "INFO";
            }

            // Format the log message with colors (if supported)
            String paddedMethod = String.format("%-7s", method);
            
            // Colored components
            String coloredMethod = colorize(paddedMethod, methodColor);
            String coloredStatus = colorize(String.valueOf(status), statusColor);
            String coloredUrl = colorize(fullUrl, ANSI_WHITE);
            String coloredIp = colorize("[" + clientIp + "]", ANSI_BLUE);
            
            // Duration color
            String durationColor = duration > 1000 ? ANSI_YELLOW : (duration > 500 ? ANSI_CYAN : ANSI_GREEN);
            String coloredDuration = colorize(duration + "ms", durationColor);
            
            // Status label (only for errors)
            String coloredLabel = "";
            if (status >= 400) {
                coloredLabel = " " + colorize("[" + statusLabel + "]", statusColor);
            }

            // LOG usando el formato con {} (como en el ejemplo que funcionaba)
            // Este formato preserva los colores ANSI correctamente
            if (status >= 500) {
                log.error("{}{} {} - {} {} {} {}",
                        coloredMethod, 
                        coloredStatus, 
                        coloredUrl,
                        coloredIp, 
                        coloredDuration,
                        coloredLabel,
                        ""); // Placeholder extra para evitar errores de formato
                
                // También log.error sin colores para el stack trace
                log.error("Request failed with status: {} - URL: {}", status, fullUrl);
                
            } else if (status >= 400) {
                log.warn("{}{} {} - {} {} {}",
                        coloredMethod, 
                        coloredStatus, 
                        coloredUrl,
                        coloredIp, 
                        coloredDuration,
                        coloredLabel);
                        
                log.warn("Request failed with status: {} - URL: {}", status, fullUrl);
                
            } else {
                log.info("{}{} {} - {} {}",
                        coloredMethod, 
                        coloredStatus, 
                        coloredUrl,
                        coloredIp, 
                        coloredDuration);
            }
        }
    }

    /**
     * Get client IP address from request headers
     * Handles proxy and load balancer scenarios
     * 
     * @param request The HTTP request
     * @return Client IP address
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR"
        };
        
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // For X-Forwarded-For, get the first IP (original client)
                if (header.equals("X-Forwarded-For") && ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // Exclude Swagger, Actuator, and other static resources if needed
        String path = request.getRequestURI();
        return path.contains("/swagger-ui") ||
               path.contains("/api-docs") ||
               path.contains("/actuator") ||
               path.contains("/v3/api-docs");
    }
}