package com.levelup.payment_service.service;

import com.levelup.payment_service.dto.message.SubscriptionMessage;
import com.levelup.payment_service.model.*;
import com.levelup.payment_service.repository.RenewalRepository;
import com.levelup.payment_service.repository.TransactionRepository;
import com.levelup.payment_service.repository.UserSubscriptionPaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionWebhookService {

    private final UserSubscriptionPaymentRepository userSubscriptionPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final RenewalRepository renewalRepository;
    private final MessagePublisherService messagePublisherService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public void handleInvoicePaymentSucceeded(Event event) {
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice == null) {
                log.error("Invoice is null in webhook event");
                return;
            }

            log.info("Processing invoice payment succeeded: {}", invoice.getId());

            // Check if this is initial subscription or renewal
            UserSubscriptionPayment subscription = userSubscriptionPaymentRepository
                    .findByStripeSubscriptionId(invoice.getSubscription())
                    .orElse(null);

            if (subscription == null) {
                // Initial subscription payment
                handleInitialSubscriptionPayment(invoice);
            } else {
                // Renewal payment
                handleRenewalPayment(invoice, subscription);
            }

        } catch (Exception e) {
            log.error("Error handling invoice payment succeeded", e);
            throw new RuntimeException("Error handling invoice payment succeeded", e);
        }
    }

    @Transactional
    public void handleInvoicePaymentFailed(Event event) {
        try {
            Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
            if (invoice == null) {
                log.error("Invoice is null in webhook event");
                return;
            }

            log.info("Processing invoice payment failed: {}", invoice.getId());

            UserSubscriptionPayment subscription = userSubscriptionPaymentRepository
                    .findByStripeSubscriptionId(invoice.getSubscription())
                    .orElse(null);

            if (subscription == null) {
                // Initial subscription payment failed
                handleInitialSubscriptionPaymentFailed(invoice);
            } else {
                // Renewal payment failed
                handleRenewalPaymentFailed(invoice, subscription);
            }

        } catch (Exception e) {
            log.error("Error handling invoice payment failed", e);
            throw new RuntimeException("Error handling invoice payment failed", e);
        }
    }

    @Transactional
    public void handleSubscriptionDeleted(Event event) {
        try {
            Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
            if (subscription == null) {
                log.error("Subscription is null in webhook event");
                return;
            }

            log.info("Processing subscription deleted: {}", subscription.getId());

            UserSubscriptionPayment userSubscription = userSubscriptionPaymentRepository
                    .findByStripeSubscriptionId(subscription.getId())
                    .orElse(null);

            if (userSubscription != null
                    && userSubscription.getStatus() == UserSubscriptionPayment.SubscriptionStatus.ACTIVE) {
                // Update subscription status
                userSubscription.setStatus(UserSubscriptionPayment.SubscriptionStatus.CANCELED);
                userSubscription.setCanceledAt(
                        subscriptionService.convertTimestampToLocalDateTime(subscription.getCanceledAt()));
                userSubscription.setIsAutoRenew(false);
                userSubscriptionPaymentRepository.save(userSubscription);

                // Send cancellation messages
                sendSubscriptionCancelMessages(userSubscription);
            }

        } catch (Exception e) {
            log.error("Error handling subscription deleted", e);
            throw new RuntimeException("Error handling subscription deleted", e);
        }
    }

    private void handleInitialSubscriptionPayment(Invoice invoice) {
        // Find transaction by metadata
        Map<String, String> metadata = invoice.getSubscription() != null
                ? getSubscriptionMetadata(invoice.getSubscription())
                : Map.of();

        if (!metadata.containsKey("transaction_id")) {
            log.warn("No transaction_id found in subscription metadata");
            return;
        }

        UUID transactionId = UUID.fromString(metadata.get("transaction_id"));
        UUID userId = UUID.fromString(metadata.get("user_id"));
        UUID subscriptionPlanId = UUID.fromString(metadata.get("subscription_plan_id"));

        // Update transaction status
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
        transactionRepository.save(transaction);

        // Create UserSubscriptionPayment record
        UserSubscriptionPayment subscription = UserSubscriptionPayment.builder()
                .transaction(transaction)
                .subscriptionPlan(new SubscriptionPlan()) // Will be set by JPA
                .userId(userId)
                .stripeSubscriptionId(invoice.getSubscription())
                .stripeInvoiceId(invoice.getId())
                .stripePaymentIntentId(invoice.getPaymentIntent())
                .firstPeriodStart(subscriptionService.convertTimestampToLocalDateTime(invoice.getPeriodStart()))
                .firstPeriodEnd(subscriptionService.convertTimestampToLocalDateTime(invoice.getPeriodEnd()))
                .afterRenewalEnd(subscriptionService.convertTimestampToLocalDateTime(invoice.getPeriodEnd()))
                .isAutoRenew(true)
                .status(UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
                .build();

        // Set subscription plan (simplified - in real implementation, fetch from
        // repository)
        subscription.getSubscriptionPlan().setId(subscriptionPlanId);

        userSubscriptionPaymentRepository.save(subscription);

        // Send success messages
        sendSubscriptionSuccessMessages(subscription);
    }

    private void handleRenewalPayment(Invoice invoice, UserSubscriptionPayment subscription) {
        // Create renewal transaction
        Transaction renewalTransaction = Transaction.builder()
                .type(Transaction.TransactionType.RENEWAL)
                .amount(subscription.getTransaction().getAmount())
                .currency(subscription.getTransaction().getCurrency())
                .status(Transaction.TransactionStatus.SUCCESS)
                .build();

        transactionRepository.save(renewalTransaction);

        // Create renewal record
        Renewal renewal = Renewal.builder()
                .transaction(renewalTransaction)
                .stripeSubscriptionId(invoice.getSubscription())
                .stripeInvoiceId(invoice.getId())
                .stripePaymentIntentId(invoice.getPaymentIntent())
                .retryCount(0)
                .status(Renewal.RenewalStatus.SUCCESS)
                .build();

        renewalRepository.save(renewal);

        // Update subscription end date
        subscription.setAfterRenewalEnd(subscriptionService.convertTimestampToLocalDateTime(invoice.getPeriodEnd()));
        userSubscriptionPaymentRepository.save(subscription);

        // Send renewal success messages
        sendRenewalSuccessMessages(subscription);
    }

    private void handleInitialSubscriptionPaymentFailed(Invoice invoice) {
        // Find and update transaction if possible
        Map<String, String> metadata = getSubscriptionMetadata(invoice.getSubscription());

        if (metadata.containsKey("transaction_id")) {
            UUID transactionId = UUID.fromString(metadata.get("transaction_id"));
            Transaction transaction = transactionRepository.findById(transactionId).orElse(null);

            if (transaction != null) {
                transaction.setStatus(Transaction.TransactionStatus.FAILED);
                transactionRepository.save(transaction);
            }
        }

        // Send failure notification
        if (metadata.containsKey("user_id")) {
            UUID userId = UUID.fromString(metadata.get("user_id"));
            SubscriptionMessage message = SubscriptionMessage.builder()
                    .userId(userId)
                    .subscriptionName("Subscription")
                    .status("PAYMENT_FAILED")
                    .build();

            messagePublisherService.sendSubscriptionNotificationMessage(message);
        }
    }

    private void handleRenewalPaymentFailed(Invoice invoice, UserSubscriptionPayment subscription) {
        // Create failed renewal transaction
        Transaction renewalTransaction = Transaction.builder()
                .type(Transaction.TransactionType.RENEWAL)
                .amount(subscription.getTransaction().getAmount())
                .currency(subscription.getTransaction().getCurrency())
                .status(Transaction.TransactionStatus.FAILED)
                .build();

        transactionRepository.save(renewalTransaction);

        // Create renewal record with retry info
        Integer attemptCount = invoice.getAttemptCount() != null ? invoice.getAttemptCount().intValue() : 1;
        LocalDateTime nextAttempt = invoice.getNextPaymentAttempt() != null
                ? subscriptionService.convertTimestampToLocalDateTime(invoice.getNextPaymentAttempt())
                : null;

        Renewal renewal = Renewal.builder()
                .transaction(renewalTransaction)
                .stripeSubscriptionId(invoice.getSubscription())
                .stripeInvoiceId(invoice.getId())
                .stripePaymentIntentId(invoice.getPaymentIntent())
                .retryCount(attemptCount)
                .status(Renewal.RenewalStatus.FAILED)
                .nextPaymentAttemptAt(nextAttempt)
                .build();

        renewalRepository.save(renewal);

        // Send retry failure notification
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("RENEWAL_FAILED")
                .retryCount(attemptCount)
                .build();

        messagePublisherService.sendSubscriptionNotificationMessage(message);
    }

    private Map<String, String> getSubscriptionMetadata(String subscriptionId) {
        try {
            com.stripe.model.Subscription subscription = com.stripe.model.Subscription.retrieve(subscriptionId);
            return subscription.getMetadata();
        } catch (Exception e) {
            log.error("Error retrieving subscription metadata", e);
            return Map.of();
        }
    }

    private void sendSubscriptionSuccessMessages(UserSubscriptionPayment subscription) {
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("ACTIVE")
                .startDate(subscription.getFirstPeriodStart())
                .endDate(subscription.getAfterRenewalEnd())
                .build();

        // Send to course service for access
        messagePublisherService.sendSubscriptionMessage(message);

        // Send to notification service
        messagePublisherService.sendSubscriptionNotificationMessage(message);
    }

    private void sendRenewalSuccessMessages(UserSubscriptionPayment subscription) {
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("RENEWED")
                .endDate(subscription.getAfterRenewalEnd())
                .build();

        // Send to course service for access extension
        messagePublisherService.sendSubscriptionMessage(message);

        // Send to notification service
        messagePublisherService.sendSubscriptionNotificationMessage(message);
    }

    private void sendSubscriptionCancelMessages(UserSubscriptionPayment subscription) {
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("CANCELED")
                .cancelDate(subscription.getCanceledAt())
                .build();

        // Send to course service for access removal
        messagePublisherService.sendSubscriptionMessage(message);

        // Send to notification service
        messagePublisherService.sendSubscriptionNotificationMessage(message);
    }
}