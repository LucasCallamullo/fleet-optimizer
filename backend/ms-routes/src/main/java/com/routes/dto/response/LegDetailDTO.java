package com.routes.dto.response;

import com.routes.model.enums.LegStatus;
import java.time.LocalDateTime;

/**
 * DTO for detailed leg response.
 * Contains all leg information including origin and destination locations.
 */
public record LegDetailDTO(
    
    Long id,
    
    Integer sequence,
    
    LegStatus status,
    
    Double distanceKm,
    
    Integer durationMinutes,
    
    LocalDateTime createdAt,
    
    LocalDateTime updatedAt,
    
    LocalDateTime startedAt,
    
    LocalDateTime completedAt,
    
    Long vehicleId,
    
    Long packageId,
    
    LocationResponseDTO origin,
    
    LocationResponseDTO destination
    
) {}