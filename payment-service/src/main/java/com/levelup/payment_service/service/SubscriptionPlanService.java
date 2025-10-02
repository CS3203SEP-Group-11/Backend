package com.levelup.payment_service.service;

import com.levelup.payment_service.dto.request.SubscriptionPlanCreateRequest;
import com.levelup.payment_service.dto.response.SubscriptionPlanResponse;
import com.levelup.payment_service.model.SubscriptionPlan;
import com.levelup.payment_service.repository.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public SubscriptionPlanResponse createSubscriptionPlan(SubscriptionPlanCreateRequest request) {
        log.info("Creating subscription plan: {} with Stripe price ID: {}", request.getName(),
                request.getStripePriceId());

        SubscriptionPlan subscriptionPlan = SubscriptionPlan.builder()
                .name(request.getName())
                .amount(request.getAmount())
                .billingCycle(request.getBillingCycle())
                .stripePriceId(request.getStripePriceId())
                .features(request.getFeatures())
                .isActive(true)
                .build();

        SubscriptionPlan savedPlan = subscriptionPlanRepository.save(subscriptionPlan);
        log.info("Subscription plan created successfully with ID: {}", savedPlan.getId());

        return mapToResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionPlanResponse> getActiveSubscriptionPlans() {
        log.info("Retrieving active subscription plans");

        List<SubscriptionPlan> activePlans = subscriptionPlanRepository.findByIsActive(true);

        return activePlans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan subscriptionPlan) {
        return SubscriptionPlanResponse.builder()
                .id(subscriptionPlan.getId())
                .name(subscriptionPlan.getName())
                .amount(subscriptionPlan.getAmount())
                .billingCycle(subscriptionPlan.getBillingCycle())
                .isActive(subscriptionPlan.getIsActive())
                .features(subscriptionPlan.getFeatures())
                .stripePriceId(subscriptionPlan.getStripePriceId())
                .build();
    }
}