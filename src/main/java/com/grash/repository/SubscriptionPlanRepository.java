package com.grash.repository;

import com.grash.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPlanRepository extends JpaRepository<com.grash.model.SubscriptionPlan, Long> {
    boolean existsByCode(String code);

    Optional<SubscriptionPlan> findByCode(String code);
}
