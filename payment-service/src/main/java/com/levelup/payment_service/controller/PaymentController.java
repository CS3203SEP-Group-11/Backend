package com.levelup.payment_service.controller;

import com.levelup.payment_service.dto.request.CoursePurchaseRequest;
import com.levelup.payment_service.dto.request.SubscriptionEnrollmentRequest;
import com.levelup.payment_service.dto.response.PaymentIntentResponse;
import com.levelup.payment_service.dto.response.SubscriptionEnrollmentResponse;
import com.levelup.payment_service.service.PaymentService;
import com.levelup.payment_service.service.SubscriptionService;
import com.levelup.payment_service.model.Transaction;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.Collections;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;

    @PostMapping("/purchase-courses")
    public ResponseEntity<PaymentIntentResponse> purchaseCourses(
            @Valid @RequestBody CoursePurchaseRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received course purchase request for user: {} with courses: {}", userIdHeader,
                    request.getCourseIds());

            UUID userId = UUID.fromString(userIdHeader);
            PaymentIntentResponse response = paymentService.createCoursePurchasePayment(request, userId);

            log.info("Course purchase payment created successfully for user: {}", userId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing course purchase request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PostMapping("/subscription-enrollment")
    public ResponseEntity<SubscriptionEnrollmentResponse> enrollCoursesWithSubscription(
            @Valid @RequestBody SubscriptionEnrollmentRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {

        try {
            log.info("Received subscription enrollment request for user: {} with courses: {}",
                    userIdHeader, request.getCourseIds());

            UUID userId = UUID.fromString(userIdHeader);
            SubscriptionEnrollmentResponse response = subscriptionService.enrollCoursesWithSubscription(request,
                    userId);

            log.info("Subscription enrollment processed for user: {} with success: {}",
                    userId, response.isSuccess());

            if (response.isSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }

        } catch (IllegalArgumentException e) {
            log.error("Invalid user ID format: {}", userIdHeader);
            return ResponseEntity.badRequest().body(new SubscriptionEnrollmentResponse(
                    "Invalid user ID format", Collections.emptyList(), false));
        } catch (Exception e) {
            log.error("Error processing subscription enrollment request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SubscriptionEnrollmentResponse(
                            "Internal server error occurred", Collections.emptyList(), false));
        }
    }
  
    /**
     * Returns the revenue summary analytics.
     */
    @GetMapping("/revenue-summary")
    public ResponseEntity<java.util.Map<String, Object>> getRevenueSummary() {
        return ResponseEntity.ok(paymentService.getRevenueSummary());
    }

    /**
     * Get all transactions for admin dashboard
     */
    @GetMapping("/transactions")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAllTransactions(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(paymentService.getAllTransactions());
    }

    /**
     * Get transaction statistics for admin dashboard
     */
    @GetMapping("/transaction-stats")
    public ResponseEntity<java.util.Map<String, Object>> getTransactionStats(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(paymentService.getTransactionStats());
    }

    /**
     * Get transaction by ID
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<Transaction> getTransactionById(
            @PathVariable UUID transactionId,
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        Transaction transaction = paymentService.getTransactionById(transactionId);
        if (transaction != null) {
            return ResponseEntity.ok(transaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}