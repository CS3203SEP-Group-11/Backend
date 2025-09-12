package com.levelup.paymentservice.repository;

import com.levelup.paymentservice.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    Optional<Purchase> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Purchase> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Purchase> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, Purchase.PurchaseStatus status);

    @Query("SELECT p FROM Purchase p WHERE p.userId = :userId AND p.createdAt BETWEEN :startDate AND :endDate ORDER BY p.createdAt DESC")
    List<Purchase> findByUserIdAndDateRange(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(p) FROM Purchase p WHERE p.status = :status")
    long countByStatus(@Param("status") Purchase.PurchaseStatus status);

    @Query("SELECT p FROM Purchase p JOIN p.items pi WHERE pi.courseId = :courseId")
    List<Purchase> findByCourseId(@Param("courseId") UUID courseId);

    @Query("SELECT p FROM Purchase p JOIN p.items pi WHERE pi.instructorId = :instructorId AND p.status = 'COMPLETED'")
    List<Purchase> findCompletedPurchasesByInstructorId(@Param("instructorId") UUID instructorId);
}
