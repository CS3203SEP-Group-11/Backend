package com.levelup.payment_service.repository;

import com.levelup.payment_service.model.PendingPurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PendingPurchaseItemRepository extends JpaRepository<PendingPurchaseItem, UUID> {
    List<PendingPurchaseItem> findByStripePaymentIntentId(String stripePaymentIntentId);

    void deleteByStripePaymentIntentId(String stripePaymentIntentId);
}