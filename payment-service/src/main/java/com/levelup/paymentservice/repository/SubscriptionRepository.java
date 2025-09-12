package com.levelup.paymentservice.repository;

import com.levelup.paymentservice.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);

    List<Subscription> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Subscription> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, Subscription.SubscriptionStatus status);

    Optional<Subscription> findByUserIdAndCourseIdAndStatus(UUID userId, UUID courseId,
            Subscription.SubscriptionStatus status);

    List<Subscription> findByCourseIdAndStatus(UUID courseId, Subscription.SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.currentPeriodEnd <= :date AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsReadyForRenewal(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Subscription s WHERE s.currentPeriodEnd <= :date AND s.status = 'UNPAID'")
    List<Subscription> findExpiredUnpaidSubscriptions(@Param("date") LocalDateTime date);

    @Query("SELECT s FROM Subscription s WHERE s.trialEnd <= :date AND s.status = 'ACTIVE' AND s.trialEnd IS NOT NULL")
    List<Subscription> findSubscriptionsWithExpiredTrials(@Param("date") LocalDateTime date);

    @Query("SELECT COUNT(s) FROM Subscription s WHERE s.status = :status")
    long countByStatus(@Param("status") Subscription.SubscriptionStatus status);

    List<Subscription> findByInstructorIdAndStatus(UUID instructorId, Subscription.SubscriptionStatus status);
}
