package com.routes.model.entity;

import com.routes.model.enums.RouteStatus;
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
 * Represents a route in the logistics system.
 * A route is a collection of legs (segments) that form a complete journey.
 * 
 * A route can be planned, in progress, completed, or cancelled.
 * It contains multiple legs that are executed in sequence.
 */
@Entity
@Table(name = "routes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Route {
    
    /*
    * Primary Key
    */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Name of the route (e.g., "Buenos Aires - Córdoba").
     * Required field.
     */
    @Column(nullable = false)
    private String name;
    
    /**
     * Optional description of the route.
     * Can include additional details like route type, notes, etc.
     */
    private String description;
    
    /**
     * Current status of the route.
     * Possible values: PLANNED, IN_PROGRESS, COMPLETED, CANCELLED
     * Default: PLANNED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status = RouteStatus.PLANNED;
    
    /**
     * Total estimated distance of the route in kilometers.
     * Can be calculated from legs or manually entered.
     * Optional - can be null if not calculated.
     */
    @Column(name = "estimated_distance_km")
    private Double estimatedDistanceKm;
    
    /**
     * Total estimated duration of the route in minutes.
     * Can be calculated from legs or manually entered.
     * Optional - can be null if not calculated.
     */
    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;
    
    /**
     * Timestamp when the route was created.
     * Auto-generated when the entity is first persisted.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when the route was last updated.
     * Auto-updated by Hibernate on every change.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ================================================================
    // RELATIONSHIPS
    // ================================================================
    
    /**
     * List of legs that belong to this route.
     * One route can have many legs.
     * 
     * mappedBy = "route" - The 'route' field in Leg owns the relationship
     * cascade = CascadeType.ALL - All operations cascade to legs
     * orphanRemoval = true - If a leg is removed from the list, it's deleted
     * OrderBy = "sequence ASC" - Legs are ordered by their sequence number
     */
    @OneToMany(mappedBy = "route", orphanRemoval = true)
    @OrderBy("sequence ASC")
    private List<Leg> legs = new ArrayList<>();
}