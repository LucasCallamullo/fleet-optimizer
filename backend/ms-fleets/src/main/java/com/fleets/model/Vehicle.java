package com.fleets.model;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

// import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Represents a vehicle in the fleet (Truck, Car, Van, etc.)
 */
@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Vehicle license plate number.
     * Must be unique across all vehicles.
     */
    @Column(nullable = false, unique = true)
    private String licensePlate;
    
    @Column(name = "vehicle_year")    // Avoid the reserved word "year"
    private Integer year;
    
    /**
     * Many-to-one relationship with Category entity.
     * Multiple vehicles can belong to the same category.
     * 
     * fetch = FetchType.LAZY - The category data is loaded only when explicitly accessed.
     *                          Improves performance by avoiding unnecessary joins.
     *                          Use category.getXXX() to trigger loading.
     * 
     * @JoinColumn(name = "category_id") - Specifies the foreign key column name in the 'vehicles' table.
     *                                     This column stores the ID of the associated Category.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)    // solo por ser explicito --> optional = true
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer"})  // ← Ignora "vehicles" y los proxies de Hibernate
    private Category category;

    // ================================================================
    // PHYSICAL CAPACITIES
    // ================================================================

    @Column(name = "max_weight_kg")
    private Double maxWeightKg;  // Maximum cargo capacity in kilograms

    @Column(name = "max_volume_cbm")
    private Double maxVolumeCbm;  // Maximum volumetric capacity in cubic meters

    // ================================================================
    // EFFICIENCY AND COSTS
    // ================================================================

    @Column(name = "fuel_consumption_per_km")
    private Double fuelConsumptionPerKm;  // Fuel consumption in liters per kilometer

    @Column(name = "cost_per_km")
    private Double costPerKm;  // Operational cost per kilometer

    @Column(name = "price_per_km")
    private Double pricePerKm;  // Sale price per kilometer

    // ================================================================
    // VEHICLE STATUS
    // ================================================================

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status = VehicleStatus.AVAILABLE;  // Current vehicle state

    // ================================================================
    // Time Stamps
    // ================================================================

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}