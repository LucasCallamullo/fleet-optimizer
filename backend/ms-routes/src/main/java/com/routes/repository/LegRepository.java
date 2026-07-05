package com.routes.repository;

import com.routes.model.entity.Leg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LegRepository extends JpaRepository<Leg, Long> {
    
    List<Leg> findByRouteId(Long routeId);
}