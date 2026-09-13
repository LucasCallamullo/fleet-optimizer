package com.routes.repository;

import com.routes.model.entity.Route;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {

/**
     * Finds a route by ID and eagerly fetches all associated legs, without
     * filtering by owner. Used by admins, who can access any route.
     *
     * @param id the route ID
     * @return Optional containing the route with legs, or empty if not found
     */
    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.legs WHERE r.id = :id")
    Optional<Route> findByIdWithLegs(@Param("id") Long id);

    /**
     * Finds a route by ID and owner, eagerly fetching all associated legs.
     * Used by regular users, who can only access their own routes.
     *
     * @param id      the route ID
     * @param ownerId the owner (user ID from the JWT sub claim)
     * @return Optional containing the route with legs, or empty if not found
     *         or not owned by the given user
     */
    @Query("SELECT r FROM Route r LEFT JOIN FETCH r.legs WHERE r.id = :id AND r.ownerId = :ownerId")
    Optional<Route> findByIdWithLegsAndOwner(@Param("id") Long id,
                                             @Param("ownerId") String ownerId);

    /**
     * Returns all routes owned by a given user, with legs eagerly fetched.
     *
     * @param ownerId the owner (user ID from the JWT sub claim)
     * @return list of routes owned by the user
     */
    @Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.legs WHERE r.ownerId = :ownerId")
    List<Route> findAllByOwnerIdWithLegs(@Param("ownerId") String ownerId);

    /**
     * Returns all routes with legs eagerly fetched. Used by admins.
     *
     * @return list of all routes
     */
    @Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.legs")
    List<Route> findAllWithLegs();
}