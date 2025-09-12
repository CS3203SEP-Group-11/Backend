package com.levelup.paymentservice.controller;

import com.levelup.paymentservice.dto.CreateSubscriptionRequest;
import com.levelup.paymentservice.dto.SubscriptionResponse;
import com.levelup.paymentservice.service.SubscriptionService;
import com.levelup.paymentservice.event.SubscriptionCreatedEvent;
import com.levelup.paymentservice.service.EventPublishingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/subscriptions")
@CrossOrigin(origins = "*")
public class SubscriptionController {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private EventPublishingService eventPublishingService;

    @PostMapping
    public Mono<ResponseEntity<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        logger.info("Creating subscription for user: {} and course: {}", request.getUserId(), request.getCourseId());

        return subscriptionService.createSubscription(request)
                .map(response -> {
                    logger.info("Subscription created successfully: {}", response.getId());

                    // Publish subscription created event
                    SubscriptionCreatedEvent event = new SubscriptionCreatedEvent(
                            response.getUserId(),
                            response.getId(),
                            response.getCourseId(),
                            response.getCourseTitle(),
                            response.getInstructorId(),
                            response.getPlanType(),
                            response.getAmount(),
                            response.getCurrency(),
                            response.getCurrentPeriodStart(),
                            response.getCurrentPeriodEnd(),
                            response.getTrialEnd());

                    eventPublishingService.publishSubscriptionCreatedEvent(event);

                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorReturn(throwable -> {
                    logger.error("Failed to create subscription", throwable);
                    if (throwable instanceof IllegalArgumentException || throwable instanceof IllegalStateException) {
                        return true;
                    }
                    return false;
                }, ResponseEntity.badRequest().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/{subscriptionId}")
    public ResponseEntity<SubscriptionResponse> getSubscription(@PathVariable UUID subscriptionId) {
        logger.info("Getting subscription: {}", subscriptionId);

        Optional<SubscriptionResponse> subscription = subscriptionService.getSubscriptionById(subscriptionId);

        if (subscription.isPresent()) {
            return ResponseEntity.ok(subscription.get());
        } else {
            logger.warn("Subscription not found: {}", subscriptionId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<SubscriptionResponse>> getUserSubscriptions(@PathVariable UUID userId) {
        logger.info("Getting subscriptions for user: {}", userId);

        List<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByUserId(userId);
        return ResponseEntity.ok(subscriptions);
    }

    @PostMapping("/{subscriptionId}/cancel")
    public ResponseEntity<String> cancelSubscription(@PathVariable UUID subscriptionId,
            @RequestParam(defaultValue = "false") boolean immediately) {
        logger.info("Cancelling subscription: {} (immediately: {})", subscriptionId, immediately);

        boolean cancelled = subscriptionService.cancelSubscription(subscriptionId, immediately);

        if (cancelled) {
            String message = immediately ? "Subscription cancelled immediately"
                    : "Subscription will be cancelled at the end of the current billing period";
            return ResponseEntity.ok(message);
        } else {
            return ResponseEntity.badRequest().body("Failed to cancel subscription");
        }
    }

    @PostMapping("/renewals/process")
    public ResponseEntity<String> processRenewals() {
        logger.info("Processing subscription renewals");

        try {
            subscriptionService.processRenewals();
            return ResponseEntity.ok("Renewals processed successfully");
        } catch (Exception e) {
            logger.error("Failed to process renewals", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process renewals");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Invalid request: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        logger.error("Invalid state: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error in subscription controller", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }
}
