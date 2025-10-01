package com.levelup.payment_service.dto.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSubscriptionMessage {
    private UUID userId;
    private boolean isSubscribed;
    private String status; // SUBSCRIBED, CANCELED, FAILED
}