package com.routes.model.entity;

import com.routes.model.enums.LegStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Represents a leg (segment) of a route.
 * A leg is a single journey from one location to another,
 * assigned to a specific vehicle.
 */
@Entity
@Table(name = "legs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Leg {
    
    // PRIMARY KEY
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Order of this leg within the route.
     * Determines the sequence of legs in the route.
     * Example: 1, 2, 3, etc.
     */
    @Column(name = "sequence", nullable = false)
    private Integer sequence;
    
    /**
     * Current status of this leg.
     * Possible values: PENDING, IN_PROGRESS, COMPLETED, CANCELLED, DELAYED
     * Default: PENDING
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegStatus status = LegStatus.PENDING;
    
    // ================================================================
    // LOCATIONS (Origin and Destination)
    // ================================================================
    
    /**
     * Origin location of the leg.
     * Embedded object - stored as columns in the legs table.
     * Column names are prefixed with "origin_".
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "origin_street")),
        @AttributeOverride(name = "streetNumber", column = @Column(name = "origin_street_number")),
        @AttributeOverride(name = "city", column = @Column(name = "origin_city")),
        @AttributeOverride(name = "state", column = @Column(name = "origin_state")),
        @AttributeOverride(name = "country", column = @Column(name = "origin_country")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "origin_postal_code")),
        @AttributeOverride(name = "latitude", column = @Column(name = "origin_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "origin_longitude"))
    })
    private Location origin;
    
    /**
     * Destination location of the leg.
     * Embedded object - stored as columns in the legs table.
     * Column names are prefixed with "destination_".
     */
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "destination_street")),
        @AttributeOverride(name = "streetNumber", column = @Column(name = "destination_street_number")),
        @AttributeOverride(name = "city", column = @Column(name = "destination_city")),
        @AttributeOverride(name = "state", column = @Column(name = "destination_state")),
        @AttributeOverride(name = "country", column = @Column(name = "destination_country")),
        @AttributeOverride(name = "postalCode", column = @Column(name = "destination_postal_code")),
        @AttributeOverride(name = "latitude", column = @Column(name = "destination_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "destination_longitude"))
    })
    private Location destination;
    
    /**
     * Estimated distance of this leg in kilometers.
     * Optional - can be calculated or provided by the user.
     */
    @Column(name = "distance_km")
    private Double distanceKm;
    
    /**
     * Estimated duration of this leg in minutes.
     * Optional - can be calculated or provided by the user.
     */
    @Column(name = "duration_minutes")
    private Integer durationMinutes;
    
    /**
     * Timestamp when this leg was created.
     * Auto-generated when the entity is first persisted.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * Timestamp when this leg was last updated.
     * Auto-updated by Hibernate on every change.
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    /**
     * Actual time when the leg started (driver departure).
     * Null if the leg hasn't started yet.
     */
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    /**
     * Actual time when the leg was completed (driver arrival).
     * Null if the leg hasn't been completed yet.
     */
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    // ================================================================
    // RELATIONSHIPS
    // ================================================================
    
    /**
     * The route that this leg belongs to.
     * Many legs can belong to one route.
     * Lazy loading to avoid unnecessary joins.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    // ================================================================
    // References others MS
    // ================================================================
    
    /**
     * ID of the vehicle assigned to this leg.
     * This is a reference to the Fleet Microservice.
     * Only the ID is stored, not the full vehicle object.
     * To get vehicle details, call Fleet MS API with this ID.
     */
    @Column(name = "vehicle_id")
    private Long vehicleId;

    /**
     * ID of the package being transported on this leg.
     * This references the Package entity in the ms-packages microservice.
     * Only the ID is stored, not the full object.
     * 
     * To get package details, call: GET /api/v1/packages/{packageId}
     * in the ms-packages service.
     */
    @Column(name = "package_id")
    private Long packageId;
}