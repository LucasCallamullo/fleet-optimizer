package com.routes.model.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a geographical location.
 * 
 * This is an EMBEDDABLE object, NOT an entity.
 * Why embeddable instead of a separate table?
 * 
 * 1. LOCATION IS PART OF THE EVENT:
 *    - Each leg has its own unique origin and destination
 *    - The location is specific to that leg and doesn't change
 *    - We don't need to share locations across different legs
 * 
 * 2. PERFORMANCE:
 *    - No joins required when querying legs
 *    - All data is stored in the legs table itself
 *    - Faster queries, better performance
 * 
 * 3. SIMPLICITY:
 *    - No need for a separate repository or service
 *    - No lifecycle management for locations
 *    - Less code to maintain
 * 
 * 4. CONSISTENCY:
 *    - If a location changes, it doesn't affect other legs
 *    - Historical data remains accurate
 *    - Each leg is self-contained
 * 
 * When to use embeddable:
 * - When the object is a value (not an entity with its own identity)
 * - When the object belongs exclusively to its parent
 * - When we don't need to query the object independently
 * 
 * When NOT to use embeddable:
 * - When the object can be shared between multiple parents
 * - When the object can change independently
 * - When we need to query the object by its own attributes
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    
    /**
     * Street name (e.g., "Av. Libertador").
     * Optional field.
     */
    private String street;
    
    /**
     * Street number (e.g., "1000").
     * Optional field - can be null if not applicable.
     */
    private String streetNumber;
    
    /**
     * City name (e.g., "Buenos Aires").
     * Optional field.
     */
    private String city;
    
    /**
     * State or province name (e.g., "CABA").
     * Optional field.
     */
    private String state;
    
    /**
     * Country name (e.g., "Argentina").
     * Optional field.
     */
    private String country;
    
    /**
     * Postal or ZIP code (e.g., "1000").
     * Optional field.
     */
    private String postalCode;
    
    /**
     * Latitude coordinate (e.g., -34.6037).
     * Optional field - used for geolocation and mapping.
     * Can be used for calculating distances between locations.
     */
    private Double latitude;
    
    /**
     * Longitude coordinate (e.g., -58.3816).
     * Optional field - used for geolocation and mapping.
     * Can be used for calculating distances between locations.
     */
    private Double longitude;
}