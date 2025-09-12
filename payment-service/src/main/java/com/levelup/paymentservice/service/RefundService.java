package com.levelup.paymentservice.service;

import com.levelup.paymentservice.model.*;
import com.levelup.paymentservice.repository.*;
import com.levelup.paymentservice.client.CourseServiceClient;
import com.stripe.exception.StripeException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefundService {

    private static final Logger logger = LoggerFactory.getLogger(RefundService.class);
    private static final int SUBSCRIPTION_REFUND_DAYS = 14;

    @Autowired
    private RefundRepository refundRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Transactional
    public Optional<Refund> requestSubscriptionRefund(UUID subscriptionId, String userReason) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        if (subscriptionOpt.isEmpty()) {
            logger.warn("Subscription not found: {}", subscriptionId);
            return Optional.empty();
        }

        Subscription subscription = subscriptionOpt.get();

        // Check refund eligibility (14-day rule)
        if (!isSubscriptionRefundEligible(subscription)) {
            logger.warn("Subscription {} is not eligible for refund", subscriptionId);
            return Optional.empty();
        }

        // Find the original transaction
        List<Transaction> transactions = transactionRepository.findByReferenceTypeAndReferenceId("SUBSCRIPTION",
                subscriptionId);
        if (transactions.isEmpty()) {
            logger.warn("No transaction found for subscription: {}", subscriptionId);
            return Optional.empty();
        }

        Transaction originalTransaction = transactions.get(0); // Get the first (original) transaction

        // Check if refund already exists
        List<Refund> existingRefunds = refundRepository.findByOriginalTransactionId(originalTransaction.getId());
        if (!existingRefunds.isEmpty()) {
            logger.warn("Refund already exists for transaction: {}", originalTransaction.getId());
            return Optional.empty();
        }

        // Create refund
        Refund refund = new Refund(
                subscription.getUserId(),
                originalTransaction.getId(),
                subscription.getAmount(), // Full refund for subscriptions
                subscription.getAmount(),
                Refund.RefundReason.SUBSCRIPTION_CANCELLATION);
        refund.setSubscriptionId(subscriptionId);
        refund.setUserReason(userReason);

        Refund savedRefund = refundRepository.save(refund);

        // Process the refund
        return processRefund(savedRefund) ? Optional.of(savedRefund) : Optional.empty();
    }

    @Transactional
    public Optional<Refund> requestPurchaseRefund(UUID purchaseId, String userReason, Refund.RefundReason reason) {
        // Note: Purchases are generally not refundable per business rules
        // This method is here for completeness but will typically deny refunds

        logger.info("Purchase refund requested for: {} - Reason: {}", purchaseId, reason);

        Optional<Purchase> purchaseOpt = purchaseRepository.findById(purchaseId);
        if (purchaseOpt.isEmpty()) {
            return Optional.empty();
        }

        Purchase purchase = purchaseOpt.get();

        // Business rule: No refunds for one-time purchases
        logger.warn("Refund denied for purchase {} - Business rule: No refunds for one-time purchases", purchaseId);
        return Optional.empty();
    }

    @Transactional
    public boolean processRefund(Refund refund) {
        try {
            // Get the original transaction to find the Stripe PaymentIntent
            Optional<Transaction> transactionOpt = transactionRepository.findById(refund.getOriginalTransactionId());
            if (transactionOpt.isEmpty()) {
                logger.error("Original transaction not found: {}", refund.getOriginalTransactionId());
                return false;
            }

            Transaction originalTransaction = transactionOpt.get();

            if (originalTransaction.getStripePaymentIntentId() == null) {
                logger.error("No Stripe PaymentIntent ID found for transaction: {}", originalTransaction.getId());
                return false;
            }

            // Create Stripe refund
            com.stripe.model.Refund stripeRefund = stripeService.createRefund(
                    originalTransaction.getStripePaymentIntentId(),
                    (long) (refund.getRefundAmount().doubleValue() * 100), // Convert to cents
                    "requested_by_customer");

            // Update refund with Stripe details
            refund.setStripeRefundId(stripeRefund.getId());
            refund.setStatus(Refund.RefundStatus.COMPLETED);
            refund.setProcessedAt(LocalDateTime.now());

            // Create refund transaction
            Transaction refundTransaction = new Transaction(
                    refund.getUserId(),
                    Transaction.TransactionType.REFUND,
                    refund.getRefundAmount().negate(), // Negative amount for refunds
                    "REFUND",
                    refund.getId());
            refundTransaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            refundTransaction.setStripePaymentIntentId(originalTransaction.getStripePaymentIntentId());

            Transaction savedRefundTransaction = transactionRepository.save(refundTransaction);
            refund.setRefundTransaction(savedRefundTransaction);
            refundRepository.save(refund);

            // Handle subscription cancellation if applicable
            if (refund.getSubscriptionId() != null) {
                handleSubscriptionRefund(refund.getSubscriptionId());
            }

            logger.info("Refund processed successfully: {} (Stripe: {})", refund.getId(), stripeRefund.getId());
            return true;

        } catch (StripeException e) {
            logger.error("Failed to process refund in Stripe", e);
            refund.setStatus(Refund.RefundStatus.FAILED);
            refund.setProcessedAt(LocalDateTime.now());
            refundRepository.save(refund);
            return false;
        }
    }

    @Transactional(readOnly = true)
    public boolean isSubscriptionRefundEligible(Subscription subscription) {
        // Business rule: Refunds only allowed within 14 days of subscription start
        LocalDateTime cutoffDate = subscription.getCreatedAt().plus(SUBSCRIPTION_REFUND_DAYS, ChronoUnit.DAYS);
        boolean withinTimeLimit = LocalDateTime.now().isBefore(cutoffDate);

        // Must be active or recently cancelled
        boolean statusEligible = subscription.getStatus() == Subscription.SubscriptionStatus.ACTIVE ||
                (subscription.getStatus() == Subscription.SubscriptionStatus.CANCELLED &&
                        subscription.getCancelledAt() != null &&
                        subscription.getCancelledAt().isAfter(cutoffDate.minusDays(1)));

        return withinTimeLimit && statusEligible;
    }

    @Transactional
    public void processAutomaticRefunds() {
        // Process any pending refunds
        List<Refund> pendingRefunds = refundRepository.findPendingRefunds();

        for (Refund refund : pendingRefunds) {
            try {
                processRefund(refund);
            } catch (Exception e) {
                logger.error("Failed to process automatic refund: {}", refund.getId(), e);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<Refund> getRefundsByUserId(UUID userId) {
        return refundRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Refund> getRefundById(UUID refundId) {
        return refundRepository.findById(refundId);
    }

    private void handleSubscriptionRefund(UUID subscriptionId) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        if (subscriptionOpt.isPresent()) {
            Subscription subscription = subscriptionOpt.get();

            // Cancel the subscription
            subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
            subscription.setCancelledAt(LocalDateTime.now());
            subscriptionRepository.save(subscription);

            // Revoke course access
            courseServiceClient.revokeCourseAccess(subscription.getUserId(), subscription.getCourseId())
                    .subscribe(
                            success -> logger.info("Course access revoked for refunded subscription: {}",
                                    subscriptionId),
                            error -> logger.error("Failed to revoke course access for refunded subscription: {}",
                                    subscriptionId, error));

            logger.info("Subscription cancelled due to refund: {}", subscriptionId);
        }
    }
}
