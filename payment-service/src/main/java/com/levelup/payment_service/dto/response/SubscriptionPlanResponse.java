package com.levelup.payment_service.dto.response;

import com.levelup.payment_service.model.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanResponse {
    private UUID id;
    private String name;
    private BigDecimal amount;
    private SubscriptionPlan.BillingCycle billingCycle;
    private Boolean isActive;
    private Map<String, Object> features;
    private String stripePriceId;
}