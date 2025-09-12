package com.levelup.paymentservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "renewals")
public class Renewal {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(name = "previous_period_end", nullable = false)
    private LocalDateTime previousPeriodEnd;

    @Column(name = "new_period_start", nullable = false)
    private LocalDateTime newPeriodStart;

    @Column(name = "new_period_end", nullable = false)
    private LocalDateTime newPeriodEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RenewalStatus status;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Renewal() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = RenewalStatus.PENDING;
        this.retryCount = 0;
    }

    public Renewal(Subscription subscription, LocalDateTime previousPeriodEnd,
            LocalDateTime newPeriodStart, LocalDateTime newPeriodEnd) {
        this();
        this.subscription = subscription;
        this.previousPeriodEnd = previousPeriodEnd;
        this.newPeriodStart = newPeriodStart;
        this.newPeriodEnd = newPeriodEnd;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }

    public LocalDateTime getPreviousPeriodEnd() {
        return previousPeriodEnd;
    }

    public void setPreviousPeriodEnd(LocalDateTime previousPeriodEnd) {
        this.previousPeriodEnd = previousPeriodEnd;
    }

    public LocalDateTime getNewPeriodStart() {
        return newPeriodStart;
    }

    public void setNewPeriodStart(LocalDateTime newPeriodStart) {
        this.newPeriodStart = newPeriodStart;
    }

    public LocalDateTime getNewPeriodEnd() {
        return newPeriodEnd;
    }

    public void setNewPeriodEnd(LocalDateTime newPeriodEnd) {
        this.newPeriodEnd = newPeriodEnd;
    }

    public RenewalStatus getStatus() {
        return status;
    }

    public void setStatus(RenewalStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == RenewalStatus.COMPLETED && processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getNextRetryAt() {
        return nextRetryAt;
    }

    public void setNextRetryAt(LocalDateTime nextRetryAt) {
        this.nextRetryAt = nextRetryAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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

    // Helper methods
    public void incrementRetry() {
        this.retryCount++;
        this.nextRetryAt = calculateNextRetryTime();
        this.updatedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        this.status = RenewalStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean canRetry() {
        return retryCount < 3 && status == RenewalStatus.PENDING;
    }

    public boolean isReadyForRetry() {
        return canRetry() && nextRetryAt != null && LocalDateTime.now().isAfter(nextRetryAt);
    }

    private LocalDateTime calculateNextRetryTime() {
        // Exponential backoff: 1 hour, 6 hours, 24 hours
        int hoursDelay = switch (retryCount) {
            case 1 -> 1;
            case 2 -> 6;
            case 3 -> 24;
            default -> 1;
        };
        return LocalDateTime.now().plusHours(hoursDelay);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enum
    public enum RenewalStatus {
        PENDING, COMPLETED, FAILED, CANCELLED
    }
}
