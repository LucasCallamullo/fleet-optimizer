package com.packages.repository;

import com.packages.model.entity.Package;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    // ================================================================
    // BASIC QUERIES
    // ================================================================

    List<Package> findByStoreId(Long storeId);
    
    List<Package> findByOwnerId(String ownerId);
    
    Optional<Package> findByTrackingNumber(String trackingNumber);

    // ================================================================
    // JOIN FETCH (eager loading for details)
    // ================================================================

    /**
     * Finds packages by IDs with their stores eagerly loaded.
     */
    @Query("SELECT p FROM Package p JOIN FETCH p.store WHERE p.id IN :ids")
    List<Package> findAllByIdWithStore(@Param("ids") List<Long> ids);

    /**
     * Finds a package by ID with its store eagerly loaded.
     * This avoids lazy loading issues when returning PackageDetailDTO.
     * This is required for mapping to PackageDTO (which needs store.location).
     */
    @Query("SELECT p FROM Package p JOIN FETCH p.store WHERE p.id = :id")
    Optional<Package> findByIdWithStore(@Param("id") Long id);

    /**
     * Finds all packages with their stores eagerly loaded.
     */
    @Query("SELECT p FROM Package p JOIN FETCH p.store")
    List<Package> findAllWithStore();

    /**
     * Finds packages by store ID with store eagerly loaded.
     */
    @Query("SELECT p FROM Package p JOIN FETCH p.store WHERE p.store.id = :storeId")
    List<Package> findByStoreIdWithStore(@Param("storeId") Long storeId);

    /**
     * Finds packages by owner ID with store eagerly loaded.
     */
    @Query("SELECT p FROM Package p JOIN FETCH p.store WHERE p.ownerId = :ownerId")
    List<Package> findByOwnerIdWithStore(@Param("ownerId") String ownerId);
}