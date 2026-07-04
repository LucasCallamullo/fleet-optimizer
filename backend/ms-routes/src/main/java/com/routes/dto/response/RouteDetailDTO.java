package com.routes.dto.response;

import com.routes.model.enums.RouteStatus;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for detailed route response.
 * Contains all route information including its legs.
 */
public record RouteDetailDTO(
    
    Long id,
    
    String name,
    
    String description,
    
    RouteStatus status,
    
    Double estimatedDistanceKm,
    
    Integer estimatedDurationMinutes,
    
    LocalDateTime createdAt,
    
    LocalDateTime updatedAt,
    
    List<LegDetailDTO> legs
    
) {}
