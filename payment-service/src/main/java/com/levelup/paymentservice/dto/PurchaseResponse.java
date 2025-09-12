package com.levelup.paymentservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PurchaseResponse {

    private UUID id;
    private UUID userId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String currency;
    private String status;
    private String stripePaymentIntentId;
    private String clientSecret;
    private LocalDateTime createdAt;
    private List<PurchaseItemResponse> items;

    // Constructors
    public PurchaseResponse() {
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(BigDecimal discountAmount) {
        this.discountAmount = discountAmount;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStripePaymentIntentId() {
        return stripePaymentIntentId;
    }

    public void setStripePaymentIntentId(String stripePaymentIntentId) {
        this.stripePaymentIntentId = stripePaymentIntentId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<PurchaseItemResponse> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItemResponse> items) {
        this.items = items;
    }

    // Inner class for purchase items
    public static class PurchaseItemResponse {
        private UUID courseId;
        private String courseTitle;
        private UUID instructorId;
        private String instructorName;
        private BigDecimal price;
        private BigDecimal discountAmount;
        private BigDecimal finalPrice;

        // Constructors
        public PurchaseItemResponse() {
        }

        // Getters and Setters
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

        public String getInstructorName() {
            return instructorName;
        }

        public void setInstructorName(String instructorName) {
            this.instructorName = instructorName;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public BigDecimal getDiscountAmount() {
            return discountAmount;
        }

        public void setDiscountAmount(BigDecimal discountAmount) {
            this.discountAmount = discountAmount;
        }

        public BigDecimal getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(BigDecimal finalPrice) {
            this.finalPrice = finalPrice;
        }
    }
}
