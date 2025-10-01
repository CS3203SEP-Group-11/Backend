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
public class SubscriptionCancelResponse {
    private UUID subscriptionId;
    private String stripeSubscriptionId;
    private String status;
    private String message;
    private LocalDateTime canceledAt;
    private String failureReason;
    private boolean success;
}