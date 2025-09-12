package com.levelup.paymentservice.controller;

import com.levelup.paymentservice.model.Transaction;
import com.levelup.paymentservice.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/payments/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    private static final Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @GetMapping("/{transactionId}")
    public ResponseEntity<Transaction> getTransaction(@PathVariable UUID transactionId) {
        logger.info("Getting transaction: {}", transactionId);

        Optional<Transaction> transaction = transactionService.getTransactionById(transactionId);

        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            logger.warn("Transaction not found: {}", transactionId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/stripe/{stripePaymentIntentId}")
    public ResponseEntity<Transaction> getTransactionByStripeId(@PathVariable String stripePaymentIntentId) {
        logger.info("Getting transaction by Stripe PaymentIntent ID: {}", stripePaymentIntentId);

        Optional<Transaction> transaction = transactionService
                .getTransactionByStripePaymentIntentId(stripePaymentIntentId);

        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            logger.warn("Transaction not found for Stripe PaymentIntent: {}", stripePaymentIntentId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Transaction>> getUserTransactions(@PathVariable UUID userId) {
        logger.info("Getting transactions for user: {}", userId);

        List<Transaction> transactions = transactionService.getTransactionsByUserId(userId);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<List<Transaction>> getUserTransactionsByStatus(@PathVariable UUID userId,
            @PathVariable Transaction.TransactionStatus status) {
        logger.info("Getting transactions for user: {} with status: {}", userId, status);

        List<Transaction> transactions = transactionService.getTransactionsByUserIdAndStatus(userId, status);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/user/{userId}/date-range")
    public ResponseEntity<List<Transaction>> getUserTransactionsByDateRange(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {

        logger.info("Getting transactions for user: {} between {} and {}", userId, startDate, endDate);

        List<Transaction> transactions = transactionService.getTransactionsByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(transactions);
    }

    @GetMapping("/reference/{referenceType}/{referenceId}")
    public ResponseEntity<List<Transaction>> getTransactionsByReference(@PathVariable String referenceType,
            @PathVariable UUID referenceId) {
        logger.info("Getting transactions for reference type: {} and ID: {}", referenceType, referenceId);

        List<Transaction> transactions = transactionService.getTransactionsByReference(referenceType, referenceId);
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{transactionId}/status/{status}")
    public ResponseEntity<Transaction> updateTransactionStatus(@PathVariable UUID transactionId,
            @PathVariable Transaction.TransactionStatus status) {
        logger.info("Updating transaction {} status to: {}", transactionId, status);

        try {
            Transaction updatedTransaction = transactionService.updateTransactionStatus(transactionId, status);
            return ResponseEntity.ok(updatedTransaction);
        } catch (IllegalArgumentException e) {
            logger.warn("Transaction not found: {}", transactionId);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/analytics/count/{status}")
    public ResponseEntity<Long> getTransactionCountByStatus(@PathVariable Transaction.TransactionStatus status) {
        logger.info("Getting transaction count for status: {}", status);

        long count = transactionService.getTransactionCountByStatus(status);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/type/{type}/status/{status}")
    public ResponseEntity<List<Transaction>> getTransactionsByTypeAndStatus(
            @PathVariable Transaction.TransactionType type,
            @PathVariable Transaction.TransactionStatus status) {
        logger.info("Getting transactions for type: {} and status: {}", type, status);

        List<Transaction> transactions = transactionService.getTransactionsByTypeAndStatus(type, status);
        return ResponseEntity.ok(transactions);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception e) {
        logger.error("Unexpected error in transaction controller", e);
        return ResponseEntity.status(500).body("An unexpected error occurred");
    }
}
