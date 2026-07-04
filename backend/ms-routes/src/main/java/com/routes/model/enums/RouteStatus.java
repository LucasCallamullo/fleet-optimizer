package com.routes.model.enums;

/**
 * Represents the status of a route.
 */
public enum RouteStatus {
    
    /**
     * Route has been planned but not yet started.
     */
    PLANNED,
    
    /**
     * Route is currently in progress (at least one leg is active).
     */
    IN_PROGRESS,
    
    /**
     * All legs of the route have been completed.
     */
    COMPLETED,
    
    /**
     * Route has been cancelled and will not be executed.
     */
    CANCELLED
}