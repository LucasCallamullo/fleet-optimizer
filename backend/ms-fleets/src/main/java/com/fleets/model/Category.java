package com.fleets.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents a vehicle category (e.g., Truck, Car, Motorcycle)
 */
@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active = true;
    
    /**
     * One-to-many relationship with Vehicle entity.    LAZY DEFAULT
     * 
     * mappedBy = "category" - The 'category' field in Vehicle owns the relationship.
     *                         Foreign key exists in 'vehicles' table.
     * 
     * cascade = {CascadeType.PERSIST, CascadeType.MERGE} - Operations propagate save/update,
     *                                                     but NOT delete. Removing a Category
     *                                                     will NOT delete its Vehicles.
     * 
     * orphanRemoval = false - Vehicles removed from this list will NOT be deleted.
     *                         They stay in database with category_id = NULL.
     */
    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = false)
    @JsonIgnore
    private List<Vehicle> vehicles = new ArrayList<>();
}