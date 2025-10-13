package com.levelup.payment_service.controller;

import com.levelup.payment_service.dto.request.CreateSubscriptionRequest;
import com.levelup.payment_service.dto.response.SubscriptionResponse;
import com.levelup.payment_service.dto.response.SubscriptionCancelResponse;
import com.levelup.payment_service.dto.response.SubscriptionRefundResponse;
import com.levelup.payment_service.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/me")
    public ResponseEntity<SubscriptionResponse> getUserSubscription(
            @RequestHeader("X-User-Id") UUID currentUserId) {
        SubscriptionResponse response = subscriptionService.getUserSubscription(currentUserId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<SubscriptionCancelResponse> cancelSubscription(
            @PathVariable UUID subscriptionId,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received cancellation request for subscription: {} from user: {}", subscriptionId, userIdHeader);

            UUID userId = UUID.fromString(userIdHeader);
            SubscriptionCancelResponse response = subscriptionService.cancelSubscription(subscriptionId, userId);

            log.info("Subscription cancellation processed: {}", subscriptionId);

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().body(SubscriptionCancelResponse.builder()
                    .subscriptionId(subscriptionId)
                    .status("INVALID_REQUEST")
                    .message("Invalid user ID format")
                    .success(false)
                    .build());
        } catch (Exception e) {
            log.error("Error processing subscription cancellation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SubscriptionCancelResponse.builder()
                            .subscriptionId(subscriptionId)
                            .status("ERROR")
                            .message("Internal server error")
                            .failureReason(e.getMessage())
                            .success(false)
                            .build());
        }
    }

    @PostMapping("/{subscriptionId}/refund")
    public ResponseEntity<SubscriptionRefundResponse> refundSubscription(
            @PathVariable UUID subscriptionId,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received refund request for subscription: {} from user: {}", subscriptionId, userIdHeader);

            UUID userId = UUID.fromString(userIdHeader);
            SubscriptionRefundResponse response = subscriptionService.refundSubscription(subscriptionId, userId);

            log.info("Subscription refund processed: {} with status: {}", subscriptionId, response.getStatus());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().body(
                    SubscriptionRefundResponse.builder()
                            .subscriptionId(subscriptionId)
                            .status("REFUND_FAILED")
                            .message("Invalid user ID format")
                            .failureReason("Invalid UUID format")
                            .success(false)
                            .build());
        } catch (Exception e) {
            log.error("Error processing subscription refund", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(SubscriptionRefundResponse.builder()
                            .subscriptionId(subscriptionId)
                            .status("REFUND_FAILED")
                            .message("Internal server error")
                            .failureReason(e.getMessage())
                            .success(false)
                            .build());
        }
    }
}