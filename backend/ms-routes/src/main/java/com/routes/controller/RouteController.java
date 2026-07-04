package com.routes.controller;

import com.routes.dto.request.RouteRequestDTO;
import com.routes.dto.response.RouteDetailDTO;
import com.routes.mapper.RouteMapper;
import com.routes.model.entity.Route;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for Route operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/routes")
@RequiredArgsConstructor
public class RouteController {

    private final RouteMapper routeMapper;

    /**
     * Creates a new route.
     * 
     * This is a temporary endpoint for testing the DTO structure.
     * It only maps the request to entity and then to response without persistence.
     * 
     * @param request The route creation request
     * @return The mapped route detail response
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RouteDetailDTO createRoute(@Valid @RequestBody RouteRequestDTO request) {
        log.info("Received route creation request with {} legs", request.legs().size());
        
        // Step 1: Log request details
        log.debug("Route name: {}", request.name());
        log.debug("Number of legs: {}", request.legs().size());
        
        // Step 2: Map DTO to Entity (temporary)
        Route route = routeMapper.toEntity(request);
        
        // Step 3: Map Entity to Response DTO
        RouteDetailDTO response = routeMapper.toDetailDto(route);
        
        log.info("Route mapped successfully to response DTO");
        
        return response;
    }
}