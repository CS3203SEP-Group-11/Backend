package com.levelup.payment_service.dto.request;

import com.levelup.payment_service.model.SubscriptionPlan;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPlanCreateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Billing cycle is required")
    private SubscriptionPlan.BillingCycle billingCycle;

    @NotBlank(message = "Stripe price ID is required")
    private String stripePriceId;

    private Map<String, Object> features;
}