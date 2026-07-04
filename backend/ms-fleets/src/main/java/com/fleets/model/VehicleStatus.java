package com.fleets.model;

/**
 * Represents the operational status of a vehicle in the fleet.
 * Used to determine vehicle availability for assignment and tracking.
 */
public enum VehicleStatus {
    
    /**
     * Vehicle is available for assignment to a new trip or route.
     * Default state for new vehicles.
     */
    AVAILABLE,
    
    /**
     * Vehicle is currently on an active trip or route.
     * Cannot be assigned to new trips until status changes.
     */
    IN_TRANSIT,
    
    /**
     * Vehicle is under maintenance or repair.
     * Temporarily unavailable for assignments.
     */
    MAINTENANCE,
    
    /**
     * Vehicle is temporarily out of service for non-maintenance reasons.
     * Examples: waiting for parts, administrative hold, etc.
     */
    OUT_OF_SERVICE,
    
    /**
     * Vehicle is reserved for a specific upcoming trip.
     * Not available for general assignment.
     */
    RESERVED
}