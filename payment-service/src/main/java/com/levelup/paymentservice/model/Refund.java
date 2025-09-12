package com.levelup.paymentservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "refunds")
public class Refund {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "original_transaction_id", nullable = false)
    private UUID originalTransactionId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "purchase_id")
    private UUID purchaseId;

    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "original_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalAmount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false)
    private RefundReason reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RefundStatus status;

    @Column(name = "stripe_refund_id", length = 255)
    private String stripeRefundId;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "user_reason", columnDefinition = "TEXT")
    private String userReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "refund_transaction_id")
    private Transaction refundTransaction;

    // Constructors
    public Refund() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RefundStatus.PENDING;
        this.currency = "USD";
    }

    public Refund(UUID userId, UUID originalTransactionId, BigDecimal refundAmount,
            BigDecimal originalAmount, RefundReason reason) {
        this();
        this.userId = userId;
        this.originalTransactionId = originalTransactionId;
        this.refundAmount = refundAmount;
        this.originalAmount = originalAmount;
        this.reason = reason;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(UUID originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public UUID getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(UUID purchaseId) {
        this.purchaseId = purchaseId;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public BigDecimal getOriginalAmount() {
        return originalAmount;
    }

    public void setOriginalAmount(BigDecimal originalAmount) {
        this.originalAmount = originalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public RefundReason getReason() {
        return reason;
    }

    public void setReason(RefundReason reason) {
        this.reason = reason;
    }

    public RefundStatus getStatus() {
        return status;
    }

    public void setStatus(RefundStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == RefundStatus.COMPLETED && processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public String getStripeRefundId() {
        return stripeRefundId;
    }

    public void setStripeRefundId(String stripeRefundId) {
        this.stripeRefundId = stripeRefundId;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }

    public String getUserReason() {
        return userReason;
    }

    public void setUserReason(String userReason) {
        this.userReason = userReason;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Transaction getRefundTransaction() {
        return refundTransaction;
    }

    public void setRefundTransaction(Transaction refundTransaction) {
        this.refundTransaction = refundTransaction;
    }

    // Helper methods
    public boolean isPartialRefund() {
        return refundAmount.compareTo(originalAmount) < 0;
    }

    public boolean isFullRefund() {
        return refundAmount.compareTo(originalAmount) == 0;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum RefundReason {
        CUSTOMER_REQUEST, SUBSCRIPTION_CANCELLATION, FRAUD, DISPUTE, COURSE_UNAVAILABLE, OTHER
    }

    public enum RefundStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}
