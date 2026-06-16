package com.fleets.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    // Colores ANSI para los Métodos HTTP
    private static final Map<String, String> METHOD_COLORS = Map.of(
            "GET", "\u001B[32m",    // Verde
            "POST", "\u001B[36m",   // Cian
            "PUT", "\u001B[33m",    // Amarillo
            "PATCH", "\u001B[35m",  // Magenta
            "DELETE", "\u001B[31m"  // Rojo
    );
    private static final String RESET_COLOR = "\u001B[0m";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Capturar tiempo de inicio, método y URL (Equivalente al inicio del middleware)
        long start = System.currentTimeMillis();
        String method = request.getMethod();
        String url = request.getRequestURI();

        String methodColor = METHOD_COLORS.getOrDefault(method, RESET_COLOR);

        try {
            // 2. PASS CONTROL TO NEXT MIDDLEWARE (Equivalente al next() de Express)
            // Esto ejecuta tu controlador y genera la respuesta
            filterChain.doFilter(request, response);
        } finally {
            // 3. RESPONSE EVENT LISTENER (Equivalente al res.on('finish'))
            // El bloque 'finally' asegura que esto se ejecute SIEMPRE, incluso si hay un error.
            long duration = System.currentTimeMillis() - start;
            int status = response.getStatus();

            // Colores ANSI para los Status Codes
            String statusColor;
            if (status >= 500) {
                statusColor = "\u001B[31m"; // Rojo - Errores de Servidor
            } else if (status >= 400) {
                statusColor = "\u001B[33m"; // Amarillo - Errores de Cliente
            } else if (status >= 300) {
                statusColor = "\u001B[36m"; // Cian - Redirecciones
            } else {
                statusColor = "\u001B[32m"; // Verde - Éxito
            }

            // Formatear el método para que ocupe 6 caracteres (padEnd en JS)
            String paddedMethod = String.format("%-6s", method);

            // Loggear usando SLF4J (respetará el patrón que pusiste en tu properties)
            // Nota: Como tu patrón ya incluye la fecha por defecto (%d), no hace falta hardcodear el IsString de la fecha aquí.
            log.info("{}{} {} {}{} ({}ms)",
                    methodColor, paddedMethod, url,
                    statusColor, status, duration);
        }
    }
}