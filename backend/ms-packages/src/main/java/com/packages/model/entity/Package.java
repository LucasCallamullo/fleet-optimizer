package com.packages.model.entity;

import com.packages.model.enums.PackageStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a package in the logistics system.
 * 
 * A package is the unit of shipment that needs to be transported
 * from a store (origin) to a customer's destination.
 * 
 * Each package belongs to a store and has a status that tracks
 * its journey through the logistics pipeline.
 */
@Entity
@Table(name = "packages")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Package {
    
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
     * Unique tracking number for the package.
     * Format: PKG-{timestamp}-{random}
     * Example: PKG-20250706-ABC123
     */
    @Column(name = "tracking_number", nullable = false, unique = true)
    private String trackingNumber;
    
    // ================================================================
    // RELATIONSHIPS
    // ================================================================
    
    /**
     * Store that owns/created this package.
     * A package belongs to exactly one store.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id", nullable = false)
    private Store store;
    
    // ================================================================
    // CAPACITY INFORMATION (for routing and validation)
    // ================================================================
    
    /**
     * Total weight of the package in kilograms.
     * Used for vehicle capacity validation.
     */
    @Column(name = "total_weight_kg")
    private Double totalWeightKg;
    
    /**
     * Total volume of the package in cubic meters.
     * Used for vehicle capacity validation.
     */
    @Column(name = "total_volume_cbm")
    private Double totalVolumeCbm;
    
    // ================================================================
    // OWNERSHIP (Keycloak user reference)
    // ================================================================
    
    /**
     * ID of the user who owns this package.
     * This references the Keycloak user ID (sub claim).
     * Example: "811bbc42-afd8-4e58-ab51-5cb97c9ec347"
     */
    @Column(name = "owner_id")
    private String ownerId;
    
    // ================================================================
    // STATUS
    // ================================================================
    
    /**
     * Current status of the package.
     * Default: CREATED
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PackageStatus status = PackageStatus.CREATED;
    
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