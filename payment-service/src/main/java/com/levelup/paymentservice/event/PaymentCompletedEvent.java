package com.levelup.paymentservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PaymentCompletedEvent {

    private UUID userId;
    private UUID purchaseId;
    private List<UUID> courseIds;
    private BigDecimal totalAmount;
    private BigDecimal finalAmount;
    private String currency;
    private String paymentMethod;
    private LocalDateTime completedAt;

    public PaymentCompletedEvent() {
    }

    public PaymentCompletedEvent(UUID userId, UUID purchaseId, List<UUID> courseIds,
            BigDecimal totalAmount, BigDecimal finalAmount, String currency) {
        this.userId = userId;
        this.purchaseId = purchaseId;
        this.courseIds = courseIds;
        this.totalAmount = totalAmount;
        this.finalAmount = finalAmount;
        this.currency = currency;
        this.paymentMethod = "STRIPE";
        this.completedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(UUID purchaseId) {
        this.purchaseId = purchaseId;
    }

    public List<UUID> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<UUID> courseIds) {
        this.courseIds = courseIds;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
