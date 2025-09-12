package com.levelup.paymentservice.repository;

import com.levelup.paymentservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Optional<Transaction> findByStripePaymentIntentId(String stripePaymentIntentId);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<Transaction> findByUserIdAndStatusOrderByCreatedAtDesc(UUID userId, Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.userId = :userId AND t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<Transaction> findByUserIdAndDateRange(@Param("userId") UUID userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    List<Transaction> findByReferenceTypeAndReferenceId(String referenceType, UUID referenceId);

    @Query("SELECT COUNT(t) FROM Transaction t WHERE t.status = :status")
    long countByStatus(@Param("status") Transaction.TransactionStatus status);

    @Query("SELECT t FROM Transaction t WHERE t.type = :type AND t.status = :status ORDER BY t.createdAt DESC")
    List<Transaction> findByTypeAndStatus(@Param("type") Transaction.TransactionType type,
            @Param("status") Transaction.TransactionStatus status);
}
