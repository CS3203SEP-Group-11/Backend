package com.levelup.paymentservice.controller;

import com.levelup.paymentservice.model.Refund;
import com.levelup.paymentservice.service.RefundService;
import com.levelup.paymentservice.event.RefundProcessedEvent;
import com.levelup.paymentservice.service.EventPublishingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/refunds")
@CrossOrigin(origins = "*")
public class RefundController {

    private static final Logger logger = LoggerFactory.getLogger(RefundController.class);

    @Autowired
    private RefundService refundService;

    @Autowired
    private EventPublishingService eventPublishingService;

    @PostMapping("/subscription/{subscriptionId}")
    public ResponseEntity<String> requestSubscriptionRefund(@PathVariable UUID subscriptionId,
            @RequestBody RefundRequest request) {
        logger.info("Refund requested for subscription: {}", subscriptionId);

        Optional<Refund> refund = refundService.requestSubscriptionRefund(subscriptionId, request.getReason());

        if (refund.isPresent()) {
            // Publish refund processed event
            Refund refundEntity = refund.get();
            RefundProcessedEvent event = new RefundProcessedEvent(
                    refundEntity.getUserId(),
                    refundEntity.getId(),
                    refundEntity.getOriginalTransactionId(),
                    refundEntity.getSubscriptionId(),
                    refundEntity.getPurchaseId(),
                    refundEntity.getRefundAmount(),
                    refundEntity.getOriginalAmount(),
                    refundEntity.getCurrency(),
                    refundEntity.getReason().name(),
                    refundEntity.getStatus().name());

            eventPublishingService.publishRefundProcessedEvent(event);
            eventPublishingService.publishCourseAccessRevokedEvent(event);

            return ResponseEntity.ok("Refund processed successfully");
        } else {
            return ResponseEntity.badRequest().body("Refund request denied or failed");
        }
    }

    @PostMapping("/purchase/{purchaseId}")
    public ResponseEntity<String> requestPurchaseRefund(@PathVariable UUID purchaseId,
            @RequestBody RefundRequest request) {
        logger.info("Refund requested for purchase: {}", purchaseId);

        // Parse the reason enum
        Refund.RefundReason reason = Refund.RefundReason.OTHER;
        try {
            reason = Refund.RefundReason.valueOf(request.getReasonCode().toUpperCase());
        } catch (Exception e) {
            logger.warn("Invalid reason code: {}, using OTHER", request.getReasonCode());
        }

        Optional<Refund> refund = refundService.requestPurchaseRefund(purchaseId, request.getReason(), reason);

        if (refund.isPresent()) {
            // Publish refund processed event
            Refund refundEntity = refund.get();
            RefundProcessedEvent event = new RefundProcessedEvent(
                    refundEntity.getUserId(),
                    refundEntity.getId(),
                    refundEntity.getOriginalTransactionId(),
                    refundEntity.getSubscriptionId(),
                    refundEntity.getPurchaseId(),
                    refundEntity.getRefundAmount(),
                    refundEntity.getOriginalAmount(),
                    refundEntity.getCurrency(),
                    refundEntity.getReason().name(),
                    refundEntity.getStatus().name());

            eventPublishingService.publishRefundProcessedEvent(event);

            return ResponseEntity.ok("Refund processed successfully");
        } else {
            return ResponseEntity.badRequest()
                    .body("Refund request denied - Business policy: No refunds for one-time purchases");
        }
    }

    @GetMapping("/{refundId}")
    public ResponseEntity<Refund> getRefund(@PathVariable UUID refundId) {
        logger.info("Getting refund: {}", refundId);

        Optional<Refund> refund = refundService.getRefundById(refundId);

        if (refund.isPresent()) {
            return ResponseEntity.ok(refund.get());
        } else {
            logger.warn("Refund not found: {}", refundId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Refund>> getUserRefunds(@PathVariable UUID userId) {
        logger.info("Getting refunds for user: {}", userId);

        List<Refund> refunds = refundService.getRefundsByUserId(userId);
        return ResponseEntity.ok(refunds);
    }

    @PostMapping("/process-automatic")
    public ResponseEntity<String> processAutomaticRefunds() {
        logger.info("Processing automatic refunds");

        try {
            refundService.processAutomaticRefunds();
            return ResponseEntity.ok("Automatic refunds processed successfully");
        } catch (Exception e) {
            logger.error("Failed to process automatic refunds", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process automatic refunds");
        }
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error in refund controller", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred");
    }

    // Inner class for request body
    public static class RefundRequest {
        private String reason;
        private String reasonCode;

        public RefundRequest() {
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public String getReasonCode() {
            return reasonCode;
        }

        public void setReasonCode(String reasonCode) {
            this.reasonCode = reasonCode;
        }
    }
}
