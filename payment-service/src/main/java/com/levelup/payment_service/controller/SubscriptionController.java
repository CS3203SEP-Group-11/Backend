package com.levelup.payment_service.controller;

import com.levelup.payment_service.dto.request.CreateSubscriptionRequest;
import com.levelup.payment_service.dto.response.SubscriptionResponse;
import com.levelup.payment_service.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping("/create")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received subscription creation request for user: {} with plan: {}", userIdHeader,
                    request.getSubscriptionPlanId());

            UUID userId = UUID.fromString(userIdHeader);
            SubscriptionResponse response = subscriptionService.createSubscription(request, userId);

            log.info("Subscription created successfully for user: {}", userId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing subscription creation request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<Map<String, Object>> cancelSubscription(
            @PathVariable UUID subscriptionId,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received cancellation request for subscription: {} from user: {}", subscriptionId, userIdHeader);

            UUID userId = UUID.fromString(userIdHeader);
            subscriptionService.cancelSubscription(subscriptionId, userId);

            log.info("Subscription canceled successfully: {}", subscriptionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Subscription canceled successfully"));

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing subscription cancellation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/{subscriptionId}/refund")
    public ResponseEntity<Map<String, Object>> refundSubscription(
            @PathVariable UUID subscriptionId,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received refund request for subscription: {} from user: {}", subscriptionId, userIdHeader);

            UUID userId = UUID.fromString(userIdHeader);
            subscriptionService.refundSubscription(subscriptionId, userId);

            log.info("Subscription refunded successfully: {}", subscriptionId);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Subscription refunded successfully"));

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing subscription refund", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}