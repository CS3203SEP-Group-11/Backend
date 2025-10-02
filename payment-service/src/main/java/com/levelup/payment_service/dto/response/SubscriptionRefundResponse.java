package com.levelup.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionRefundResponse {
    private UUID subscriptionId;
    private String stripeSubscriptionId;
    private String stripeRefundId;
    private String status;
    private String message;
    private LocalDateTime refundedAt;
    private String failureReason;
    private boolean success;
}