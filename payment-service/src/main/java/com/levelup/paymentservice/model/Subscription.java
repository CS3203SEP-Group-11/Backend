package com.levelup.paymentservice.model;

import jakarta.persistence.*;
import org.hibernate.annotations.GenericGenerator;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    @Column(name = "instructor_id", nullable = false)
    private UUID instructorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_type", nullable = false)
    private PlanType planType;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "stripe_subscription_id", length = 255)
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id", length = 255)
    private String stripeCustomerId;

    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "trial_start")
    private LocalDateTime trialStart;

    @Column(name = "trial_end")
    private LocalDateTime trialEnd;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SubscriptionPayment> payments;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Renewal> renewals;

    // Constructors
    public Subscription() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = SubscriptionStatus.ACTIVE;
        this.currency = "USD";
    }

    public Subscription(UUID userId, UUID courseId, String courseTitle,
            UUID instructorId, PlanType planType, BigDecimal amount) {
        this();
        this.userId = userId;
        this.courseId = courseId;
        this.courseTitle = courseTitle;
        this.instructorId = instructorId;
        this.planType = planType;
        this.amount = amount;
        this.currentPeriodStart = LocalDateTime.now();
        this.currentPeriodEnd = calculateNextPeriodEnd();
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

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
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

    public SubscriptionStatus getStatus() {
        return status;
    }

    public void setStatus(SubscriptionStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
        if (status == SubscriptionStatus.CANCELLED && cancelledAt == null) {
            this.cancelledAt = LocalDateTime.now();
        }
    }

    public String getStripeSubscriptionId() {
        return stripeSubscriptionId;
    }

    public void setStripeSubscriptionId(String stripeSubscriptionId) {
        this.stripeSubscriptionId = stripeSubscriptionId;
    }

    public String getStripeCustomerId() {
        return stripeCustomerId;
    }

    public void setStripeCustomerId(String stripeCustomerId) {
        this.stripeCustomerId = stripeCustomerId;
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

    public LocalDateTime getTrialStart() {
        return trialStart;
    }

    public void setTrialStart(LocalDateTime trialStart) {
        this.trialStart = trialStart;
    }

    public LocalDateTime getTrialEnd() {
        return trialEnd;
    }

    public void setTrialEnd(LocalDateTime trialEnd) {
        this.trialEnd = trialEnd;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(LocalDateTime cancelledAt) {
        this.cancelledAt = cancelledAt;
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

    public List<SubscriptionPayment> getPayments() {
        return payments;
    }

    public void setPayments(List<SubscriptionPayment> payments) {
        this.payments = payments;
    }

    public List<Renewal> getRenewals() {
        return renewals;
    }

    public void setRenewals(List<Renewal> renewals) {
        this.renewals = renewals;
    }

    // Helper methods
    private LocalDateTime calculateNextPeriodEnd() {
        if (planType == PlanType.MONTHLY) {
            return currentPeriodStart.plusMonths(1);
        } else if (planType == PlanType.ANNUAL) {
            return currentPeriodStart.plusYears(1);
        }
        return currentPeriodStart.plusMonths(1); // Default to monthly
    }

    public void renewSubscription() {
        this.currentPeriodStart = this.currentPeriodEnd;
        this.currentPeriodEnd = calculateNextPeriodEnd();
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE;
    }

    public boolean isInTrial() {
        return trialEnd != null && LocalDateTime.now().isBefore(trialEnd);
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enums
    public enum PlanType {
        MONTHLY, ANNUAL
    }

    public enum SubscriptionStatus {
        ACTIVE, CANCELLED, PAUSED, EXPIRED, UNPAID
    }
}
