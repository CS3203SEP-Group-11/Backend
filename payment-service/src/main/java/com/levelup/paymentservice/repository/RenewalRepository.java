package com.levelup.paymentservice.repository;

import com.levelup.paymentservice.model.Renewal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RenewalRepository extends JpaRepository<Renewal, UUID> {

    List<Renewal> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId);

    @Query("SELECT r FROM Renewal r WHERE r.status = 'PENDING' AND r.nextRetryAt <= :currentTime")
    List<Renewal> findRenewalsReadyForRetry(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT r FROM Renewal r WHERE r.status = 'PENDING' AND r.retryCount < 3")
    List<Renewal> findPendingRenewalsWithinRetryLimit();

    @Query("SELECT r FROM Renewal r WHERE r.subscription.id = :subscriptionId AND r.status = :status ORDER BY r.createdAt DESC")
    List<Renewal> findBySubscriptionIdAndStatus(@Param("subscriptionId") UUID subscriptionId,
            @Param("status") Renewal.RenewalStatus status);

    @Query("SELECT COUNT(r) FROM Renewal r WHERE r.status = :status")
    long countByStatus(@Param("status") Renewal.RenewalStatus status);
}
