package com.levelup.payment_service.repository;

import com.levelup.payment_service.model.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    List<SubscriptionPlan> findByIsActive(Boolean isActive);

    Optional<SubscriptionPlan> findByIdAndIsActive(UUID id, Boolean isActive);
}