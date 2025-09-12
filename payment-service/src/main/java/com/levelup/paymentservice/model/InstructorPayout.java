package com.levelup.paymentservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "instructor_payouts")
public class InstructorPayout {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "gross_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "platform_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal platformFee;

    @Column(name = "instructor_share", nullable = false, precision = 10, scale = 2)
    private BigDecimal instructorShare;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PayoutStatus status;

    @Column(name = "stripe_transfer_id", length = 255)
    private String stripeTransferId;

    @Column(name = "stripe_connected_account_id", length = 255)
    private String stripeConnectedAccountId;

    @Column(name = "payout_method", nullable = false)
    private String payoutMethod = "STRIPE";

    @Column(name = "scheduled_payout_date")
    private LocalDateTime scheduledPayoutDate;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public InstructorPayout() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = PayoutStatus.PENDING;
        this.currency = "USD";
        this.payoutMethod = "STRIPE";
    }

    public InstructorPayout(UUID instructorId, UUID courseId, UUID transactionId,
            BigDecimal grossAmount, BigDecimal platformFeeRate) {
        this();
        this.instructorId = instructorId;
        this.courseId = courseId;
        this.transactionId = transactionId;
        this.grossAmount = grossAmount;
        calculatePayout(platformFeeRate);
        this.scheduledPayoutDate = LocalDateTime.now().plusDays(7); // Default 7-day delay
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(UUID instructorId) {
        this.instructorId = instructorId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public BigDecimal getGrossAmount() {
        return grossAmount;
    }

    public void setGrossAmount(BigDecimal grossAmount) {
        this.grossAmount = grossAmount;
    }

    public BigDecimal getPlatformFee() {
        return platformFee;
    }

    public void setPlatformFee(BigDecimal platformFee) {
        this.platformFee = platformFee;
    }

    public BigDecimal getInstructorShare() {
        return instructorShare;
    }

    public void setInstructorShare(BigDecimal instructorShare) {
        this.instructorShare = instructorShare;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PayoutStatus getStatus() {
        return status;
    }

    public void setStatus(PayoutStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == PayoutStatus.COMPLETED && processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }

    public String getStripeTransferId() {
        return stripeTransferId;
    }

    public void setStripeTransferId(String stripeTransferId) {
        this.stripeTransferId = stripeTransferId;
    }

    public String getStripeConnectedAccountId() {
        return stripeConnectedAccountId;
    }

    public void setStripeConnectedAccountId(String stripeConnectedAccountId) {
        this.stripeConnectedAccountId = stripeConnectedAccountId;
    }

    public String getPayoutMethod() {
        return payoutMethod;
    }

    public void setPayoutMethod(String payoutMethod) {
        this.payoutMethod = payoutMethod;
    }

    public LocalDateTime getScheduledPayoutDate() {
        return scheduledPayoutDate;
    }

    public void setScheduledPayoutDate(LocalDateTime scheduledPayoutDate) {
        this.scheduledPayoutDate = scheduledPayoutDate;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
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
    private void calculatePayout(BigDecimal platformFeeRate) {
        this.platformFee = grossAmount.multiply(platformFeeRate);
        this.instructorShare = grossAmount.subtract(platformFee);
    }

    public void recalculatePayout(BigDecimal platformFeeRate) {
        calculatePayout(platformFeeRate);
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isReadyForPayout() {
        return status == PayoutStatus.PENDING &&
                scheduledPayoutDate != null &&
                LocalDateTime.now().isAfter(scheduledPayoutDate);
    }

    public void markFailed(String reason) {
        this.status = PayoutStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enum
    public enum PayoutStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }
}
