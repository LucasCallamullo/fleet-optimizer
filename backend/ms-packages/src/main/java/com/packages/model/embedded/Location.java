package com.packages.model.embedded;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable location object. 
 * 
 * This represents a physical address with coordinates.
 * Used by Store to provide origin location for packages.
 * 
 * Why embeddable instead of entity?
 * - Location is a value object, not an entity
 * - It belongs exclusively to the Store
 * - No need to query locations independently
 * - Simpler and more performant
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    
    /**
     * Street name (e.g., "Av. Libertador")
     */
    private String street;
    
    /**
     * Street number (e.g., "1000")
     */
    private String streetNumber;
    
    /**
     * City name (e.g., "Buenos Aires")
     */
    private String city;
    
    /**
     * State or province (e.g., "CABA")
     */
    private String state;
    
    /**
     * Country name (e.g., "Argentina")
     */
    private String country;
    
    /**
     * Postal code (e.g., "1000")
     */
    private String postalCode;
    
    /**
     * Latitude coordinate (for geocoding)
     */
    private Double latitude;
    
    /**
     * Longitude coordinate (for geocoding)
     */
    private Double longitude;
}