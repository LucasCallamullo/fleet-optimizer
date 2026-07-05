package com.routes.repository;

import com.routes.model.entity.Route;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

    /**
     * Finds a route by ID and eagerly fetches all associated legs.
     * Uses JOIN FETCH to load legs in the same query.
     * 
     * r.legs some like:
        SELECT r.*, l.* 
            FROM routes r 
            LEFT JOIN legs l ON l.route_id = r.id 
            WHERE r.id = ?
     * 
     * @param id The route ID
     * @return Optional containing the route with legs, or empty if not found
     */
    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.legs WHERE r.id = :id")
    Optional<Route> findByIdWithLegs(@Param("id") Long id);
}