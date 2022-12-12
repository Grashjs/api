package com.grash.repository;

import com.grash.model.FloorPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface FloorPlanRepository extends JpaRepository<FloorPlan, Long> {
    Collection<FloorPlan> findByLocation_Id(Long id);
}
