package com.levelup.paymentservice.dto;

import com.levelup.paymentservice.model.Subscription;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class CreateSubscriptionRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Course ID is required")
    private UUID courseId;

    @NotNull(message = "Plan type is required")
    private Subscription.PlanType planType;

    private Integer trialDays;

    private String couponCode;

    // Constructors
    public CreateSubscriptionRequest() {
    }

    public CreateSubscriptionRequest(UUID userId, UUID courseId, Subscription.PlanType planType) {
        this.userId = userId;
        this.courseId = courseId;
        this.planType = planType;
    }

    // Getters and Setters
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

    public Subscription.PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(Subscription.PlanType planType) {
        this.planType = planType;
    }

    public Integer getTrialDays() {
        return trialDays;
    }

    public void setTrialDays(Integer trialDays) {
        this.trialDays = trialDays;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
