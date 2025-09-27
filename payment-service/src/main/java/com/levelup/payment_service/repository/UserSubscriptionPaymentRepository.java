package com.levelup.payment_service.repository;

import com.levelup.payment_service.model.UserSubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSubscriptionPaymentRepository extends JpaRepository<UserSubscriptionPayment, UUID> {
    Optional<UserSubscriptionPayment> findByStripeSubscriptionId(String stripeSubscriptionId);

    Optional<UserSubscriptionPayment> findByIdAndUserId(UUID id, UUID userId);

    Optional<UserSubscriptionPayment> findByUserIdAndStatus(UUID userId,
            UserSubscriptionPayment.SubscriptionStatus status);
}