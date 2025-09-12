package com.levelup.paymentservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cancellations")
public class Cancellation {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cancellation_reason", nullable = false)
    private CancellationReason cancellationReason;

    @Column(name = "user_feedback", columnDefinition = "TEXT")
    private String userFeedback;

    @Column(name = "effective_date", nullable = false)
    private LocalDateTime effectiveDate;

    @Column(name = "refund_eligible", nullable = false)
    private Boolean refundEligible = false;

    @Column(name = "prorate_refund", nullable = false)
    private Boolean prorateRefund = false;

    @Column(name = "processed_by")
    private UUID processedBy;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Cancellation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.refundEligible = false;
        this.prorateRefund = false;
    }

    public Cancellation(UUID subscriptionId, UUID userId, CancellationReason reason,
            LocalDateTime effectiveDate) {
        this();
        this.subscriptionId = subscriptionId;
        this.userId = userId;
        this.cancellationReason = reason;
        this.effectiveDate = effectiveDate;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public CancellationReason getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(CancellationReason cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public String getUserFeedback() {
        return userFeedback;
    }

    public void setUserFeedback(String userFeedback) {
        this.userFeedback = userFeedback;
    }

    public LocalDateTime getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDateTime effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Boolean getRefundEligible() {
        return refundEligible;
    }

    public void setRefundEligible(Boolean refundEligible) {
        this.refundEligible = refundEligible;
    }

    public Boolean getProrateRefund() {
        return prorateRefund;
    }

    public void setProrateRefund(Boolean prorateRefund) {
        this.prorateRefund = prorateRefund;
    }

    public UUID getProcessedBy() {
        return processedBy;
    }

    public void setProcessedBy(UUID processedBy) {
        this.processedBy = processedBy;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
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

    // Helper methods
    public boolean isImmediateCancellation() {
        return effectiveDate.isBefore(LocalDateTime.now().plusDays(1));
    }

    public boolean isScheduledCancellation() {
        return effectiveDate.isAfter(LocalDateTime.now());
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enum
    public enum CancellationReason {
        USER_REQUESTED, PAYMENT_FAILURE, COURSE_COMPLETED, DISSATISFIED,
        TECHNICAL_ISSUES, FOUND_ALTERNATIVE, TOO_EXPENSIVE, OTHER
    }
}
