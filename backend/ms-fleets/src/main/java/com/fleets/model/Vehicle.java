package com.fleets.model;

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
     * 
     * nullable = true (implied by @ManyToOne default) - The category can be null.
     *                                                     A vehicle does NOT require a category.
     *                                                     To make it required, add: @JoinColumn(nullable = false)
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)    // solo por ser explicito --> optional = true
    @JoinColumn(name = "category_id")
    @JsonIgnoreProperties({"vehicles", "hibernateLazyInitializer"})  // ← Ignora "vehicles" y los proxies de Hibernate
    // @JsonIgnore  // ← Agregar este import: com.fasterxml.jackson.annotation.JsonIgnore
    private Category category;
}