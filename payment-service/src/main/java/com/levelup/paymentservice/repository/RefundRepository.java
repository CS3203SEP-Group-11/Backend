package com.levelup.paymentservice.repository;

import com.levelup.paymentservice.model.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

    Optional<Refund> findByStripeRefundId(String stripeRefundId);

    List<Refund> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Refund> findByOriginalTransactionId(UUID originalTransactionId);

    List<Refund> findBySubscriptionIdAndStatus(UUID subscriptionId, Refund.RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.userId = :userId AND r.createdAt BETWEEN :startDate AND :endDate ORDER BY r.createdAt DESC")
    List<Refund> findByUserIdAndDateRange(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(r) FROM Refund r WHERE r.status = :status")
    long countByStatus(@Param("status") Refund.RefundStatus status);

    @Query("SELECT r FROM Refund r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
    List<Refund> findPendingRefunds();
}
