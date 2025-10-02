package com.levelup.payment_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionResponse {
    private String clientSecret;
    private String paymentIntentId;
    private String subscriptionId;
    private String planName;
    private BigDecimal amount;
    private String currency;
}