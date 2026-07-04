package com.routes.model.enums;

/**
 * Represents the status of a leg within a route.
 */
public enum LegStatus {
    
    /**
     * Leg is pending and waiting to start.
     */
    PENDING,
    
    /**
     * Leg is currently in progress.
     */
    IN_PROGRESS,
    
    /**
     * Leg has been completed successfully.
     */
    COMPLETED,
    
    /**
     * Leg has been cancelled.
     */
    CANCELLED,
    
    /**
     * Leg is delayed but still expected to complete.
     */
    DELAYED
}