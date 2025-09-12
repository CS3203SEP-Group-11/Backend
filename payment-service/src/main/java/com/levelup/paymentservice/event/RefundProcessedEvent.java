package com.levelup.paymentservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class RefundProcessedEvent {

    private UUID userId;
    private UUID refundId;
    private UUID originalTransactionId;
    private UUID subscriptionId;
    private UUID purchaseId;
    private BigDecimal refundAmount;
    private BigDecimal originalAmount;
    private String currency;
    private String reason;
    private String status;
    private LocalDateTime processedAt;

    public RefundProcessedEvent() {
    }

    public RefundProcessedEvent(UUID userId, UUID refundId, UUID originalTransactionId,
            UUID subscriptionId, UUID purchaseId, BigDecimal refundAmount,
            BigDecimal originalAmount, String currency, String reason, String status) {
        this.userId = userId;
        this.refundId = refundId;
        this.originalTransactionId = originalTransactionId;
        this.subscriptionId = subscriptionId;
        this.purchaseId = purchaseId;
        this.refundAmount = refundAmount;
        this.originalAmount = originalAmount;
        this.currency = currency;
        this.reason = reason;
        this.status = status;
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getRefundId() {
        return refundId;
    }

    public void setRefundId(UUID refundId) {
        this.refundId = refundId;
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

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }
}
