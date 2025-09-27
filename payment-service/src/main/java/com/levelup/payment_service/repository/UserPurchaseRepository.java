package com.levelup.payment_service.repository;

import com.levelup.payment_service.model.UserPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserPurchaseRepository extends JpaRepository<UserPurchase, UUID> {
    Optional<UserPurchase> findByStripePaymentIntentId(String stripePaymentIntentId);
}