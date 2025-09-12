package com.levelup.paymentservice.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class SubscriptionCreatedEvent {

    private UUID userId;
    private UUID subscriptionId;
    private UUID courseId;
    private String courseTitle;
    private UUID instructorId;
    private String planType;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private LocalDateTime trialEnd;
    private LocalDateTime createdAt;

    public SubscriptionCreatedEvent() {
    }

    public SubscriptionCreatedEvent(UUID userId, UUID subscriptionId, UUID courseId,
            String courseTitle, UUID instructorId, String planType,
            BigDecimal amount, String currency, LocalDateTime currentPeriodStart,
            LocalDateTime currentPeriodEnd, LocalDateTime trialEnd) {
        this.userId = userId;
        this.subscriptionId = subscriptionId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.instructorId = instructorId;
        this.planType = planType;
        this.amount = amount;
        this.currency = currency;
        this.currentPeriodStart = currentPeriodStart;
        this.currentPeriodEnd = currentPeriodEnd;
        this.trialEnd = trialEnd;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(UUID subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public UUID getCourseId() {
        return courseId;
    }

    public void setCourseId(UUID courseId) {
        this.courseId = courseId;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public UUID getInstructorId() {
        return instructorId;
    }

    public void setInstructorId(UUID instructorId) {
        this.instructorId = instructorId;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public LocalDateTime getCurrentPeriodStart() {
        return currentPeriodStart;
    }

    public void setCurrentPeriodStart(LocalDateTime currentPeriodStart) {
        this.currentPeriodStart = currentPeriodStart;
    }

    public LocalDateTime getCurrentPeriodEnd() {
        return currentPeriodEnd;
    }

    public void setCurrentPeriodEnd(LocalDateTime currentPeriodEnd) {
        this.currentPeriodEnd = currentPeriodEnd;
    }

    public LocalDateTime getTrialEnd() {
        return trialEnd;
    }

    public void setTrialEnd(LocalDateTime trialEnd) {
        this.trialEnd = trialEnd;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
