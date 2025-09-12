package com.levelup.paymentservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class CreatePurchaseRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Course IDs are required")
    private List<UUID> courseIds;

    private BigDecimal discountAmount = BigDecimal.ZERO;

    private String couponCode;

    // Constructors
    public CreatePurchaseRequest() {
    }

    public CreatePurchaseRequest(UUID userId, List<UUID> courseIds) {
        this.userId = userId;
        this.courseIds = courseIds;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public List<UUID> getCourseIds() {
        return courseIds;
    }

    public void setCourseIds(List<UUID> courseIds) {
        this.courseIds = courseIds;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }
}
