package com.packages.model.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Represents the status of a package throughout its lifecycle.
 * 
 * Status flow:
 * CREATED → PROCESSING → READY_FOR_PICKUP → IN_TRANSIT → DELIVERED
 *                ↓              ↓
 *            ON_HOLD       CANCELLED
 */
public enum PackageStatus {
    
    /**
     * Package has been created but not yet processed.
     * Initial state for all packages.
     */
    CREATED,
    
    /**
     * Package is being prepared/processed for shipment.
     */
    PROCESSING,
    
    /**
     * Package is ready to be picked up by the carrier.
     */
    READY_FOR_PICKUP,
    
    /**
     * Package is in transit to the destination.
     */
    IN_TRANSIT,
    
    /**
     * Package has been delivered successfully.
     */
    DELIVERED,
    
    /**
     * Package is temporarily on hold (customer request, missing info, etc.)
     */
    ON_HOLD,
    
    /**
     * Package has been cancelled and will not be delivered.
     */
    CANCELLED;

    /**
     * Helper method to get all enum names (for error messages)
     */
    public static List<String> getAllNames() {
        return Arrays.stream(values())
            .map(Enum::name)
            .toList();
    }
}