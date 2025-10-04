package com.levelup.payment_service.controller;

import com.levelup.payment_service.dto.request.SubscriptionPlanCreateRequest;
import com.levelup.payment_service.dto.response.SubscriptionPlanResponse;
import com.levelup.payment_service.service.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/subscription-plans")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @PostMapping("/create")
    public ResponseEntity<SubscriptionPlanResponse> createSubscriptionPlan(
            @Valid @RequestBody SubscriptionPlanCreateRequest request) {

        try {
            log.info("Received subscription plan creation request: {}", request.getName());
            SubscriptionPlanResponse response = subscriptionPlanService.createSubscriptionPlan(request);
            log.info("Subscription plan created successfully with ID: {}", response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating subscription plan", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<SubscriptionPlanResponse>> getActiveSubscriptionPlans() {
        try {
            log.info("Retrieving active subscription plans");
            List<SubscriptionPlanResponse> response = subscriptionPlanService.getActiveSubscriptionPlans();
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving active subscription plans", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}