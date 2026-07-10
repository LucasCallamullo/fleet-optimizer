package com.packages.model.entity;

import com.packages.model.embedded.Location;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a store or warehouse that creates packages.
 * 
 * A store is the origin point for packages in the logistics system.
 * Each store has a location that serves as the pickup point.
 * 
 * Stores can have multiple packages associated with them.
 * This allows grouping packages by store for optimization.
 */
@Entity
@Table(name = "stores")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Store {
    
    // ================================================================
    // PRIMARY KEY
    // ================================================================
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // ================================================================
    // IDENTIFICATION
    // ================================================================
    
    /**
     * Name of the store (e.g., "Downtown Warehouse", "Mall Branch")
     */
    @Column(name = "name", nullable = false)
    private String name;
    
    /**
     * Optional description or notes about the store.
     */
    @Column(name = "description")
    private String description;
    
    // ================================================================
    // LOCATION (embedded)
    // ================================================================
    
    /**
     * Physical location of the store.
     * Includes street, city, country, and coordinates.
     */
    @Embedded
    private Location location;
    
    // ================================================================
    // OWNERSHIP
    // ================================================================
    
    /**
     * ID of the user who owns/manages this store.
     * References Keycloak user ID.
     */
    @Column(name = "owner_id")
    private String ownerId;
    
    // ================================================================
    // RELATIONSHIPS
    // ================================================================
    
    /**
     * List of packages created by this store.
     * One store can have many packages.
     */
    @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Package> packages = new ArrayList<>();
    
    // ================================================================
    // TIMESTAMPS
    // ================================================================
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}