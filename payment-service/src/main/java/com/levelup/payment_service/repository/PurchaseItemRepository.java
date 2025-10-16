package com.levelup.payment_service.repository;

import com.levelup.payment_service.model.PurchaseItem;
import com.levelup.payment_service.model.UserPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, UUID> {
    List<PurchaseItem> findByUserPurchase(UserPurchase userPurchase);
}