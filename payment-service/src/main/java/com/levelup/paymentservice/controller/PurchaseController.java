package com.levelup.paymentservice.controller;

import com.levelup.paymentservice.dto.CreatePurchaseRequest;
import com.levelup.paymentservice.dto.PurchaseResponse;
import com.levelup.paymentservice.service.PurchaseService;
import com.levelup.paymentservice.event.PaymentCompletedEvent;
import com.levelup.paymentservice.service.EventPublishingService;
import com.levelup.paymentservice.model.PurchaseItem;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments/purchases")
@CrossOrigin(origins = "*")
public class PurchaseController {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private EventPublishingService eventPublishingService;

    @PostMapping
    public Mono<ResponseEntity<PurchaseResponse>> createPurchase(@Valid @RequestBody CreatePurchaseRequest request) {
        logger.info("Creating purchase request for user: {} with courses: {}", request.getUserId(),
                request.getCourseIds());

        return purchaseService.createPurchase(request)
                .map(response -> {
                    logger.info("Purchase created successfully: {}", response.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(response);
                })
                .onErrorReturn(throwable -> {
                    logger.error("Failed to create purchase for user: {}", request.getUserId(), throwable);
                    if (throwable instanceof IllegalArgumentException) {
                        return true;
                    }
                    return false;
                }, ResponseEntity.badRequest().build())
                .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @GetMapping("/{purchaseId}")
    public ResponseEntity<PurchaseResponse> getPurchase(@PathVariable UUID purchaseId) {
        logger.info("Getting purchase: {}", purchaseId);

        Optional<PurchaseResponse> purchase = purchaseService.getPurchaseById(purchaseId);

        if (purchase.isPresent()) {
            return ResponseEntity.ok(purchase.get());
        } else {
            logger.warn("Purchase not found: {}", purchaseId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PurchaseResponse>> getUserPurchases(@PathVariable UUID userId) {
        logger.info("Getting purchases for user: {}", userId);

        List<PurchaseResponse> purchases = purchaseService.getPurchasesByUserId(userId);
        return ResponseEntity.ok(purchases);
    }

    @PostMapping("/{purchaseId}/complete")
    public ResponseEntity<String> completePurchase(@PathVariable UUID purchaseId) {
        logger.info("Completing purchase: {}", purchaseId);

        Optional<PurchaseResponse> purchaseOpt = purchaseService.getPurchaseById(purchaseId);
        if (purchaseOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PurchaseResponse purchase = purchaseOpt.get();
        boolean completed = purchaseService.completePurchase(purchase.getStripePaymentIntentId());

        if (completed) {
            // Publish payment completed event
            PaymentCompletedEvent event = new PaymentCompletedEvent(
                    purchase.getUserId(),
                    purchase.getId(),
                    purchase.getItems().stream()
                            .map(PurchaseResponse.PurchaseItemResponse::getCourseId)
                            .collect(Collectors.toList()),
                    purchase.getTotalAmount(),
                    purchase.getFinalAmount(),
                    purchase.getCurrency());

            eventPublishingService.publishPaymentCompletedEvent(event);
            eventPublishingService.publishCourseAccessGrantedEvent(event);

            return ResponseEntity.ok("Purchase completed successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to complete purchase");
        }
    }

    @PostMapping("/{purchaseId}/cancel")
    public ResponseEntity<String> cancelPurchase(@PathVariable UUID purchaseId) {
        logger.info("Cancelling purchase: {}", purchaseId);

        boolean cancelled = purchaseService.cancelPurchase(purchaseId);

        if (cancelled) {
            return ResponseEntity.ok("Purchase cancelled successfully");
        } else {
            return ResponseEntity.badRequest().body("Failed to cancel purchase");
        }
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException e) {
        logger.error("Invalid request: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error in purchase controller", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }
}
