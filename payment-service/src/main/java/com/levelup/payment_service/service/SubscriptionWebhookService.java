package com.levelup.payment_service.service;

import com.levelup.payment_service.dto.message.PaymentNotificationMessage;
import com.levelup.payment_service.dto.message.SubscriptionMessage;
import com.levelup.payment_service.dto.message.UserSubscriptionMessage;
import com.levelup.payment_service.model.*;
import com.levelup.payment_service.repository.RenewalRepository;
import com.levelup.payment_service.repository.SubscriptionPlanRepository;
import com.levelup.payment_service.repository.TransactionRepository;
import com.levelup.payment_service.repository.UserSubscriptionPaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.Invoice;
import com.stripe.net.ApiResource;
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
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final MessagePublisherService messagePublisherService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public void handleInvoicePaymentSucceeded(Event event) {
        try {
            // log.info("Processing invoice.payment_succeeded event: {}", event);

            Invoice invoice;

            // Try to deserialize using EventDataObjectDeserializer first
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                invoice = (Invoice) event.getDataObjectDeserializer().getObject().get();
                log.info("This is invoice1: {}", invoice);
                log.info("Successfully deserialized Invoice using EventDataObjectDeserializer");
            } else {
                // Fallback: manually deserialize from JSON
                log.info("EventDataObjectDeserializer returned empty, using manual deserialization");
                invoice = ApiResource.GSON.fromJson(
                        event.getDataObjectDeserializer().getRawJson(),
                        Invoice.class);
                // log.info("This is invoice2: {}", invoice);
            }

            if (invoice == null) {
                log.error("Invoice is still null after deserialization attempts");
                return;
            }

            log.info("Processing invoice payment succeeded: {}", invoice.getId());

            // Extract subscription ID from the raw event JSON (since invoice.subscription
            // is null)
            String subscriptionId = extractSubscriptionIdFromEvent(event);

            if (subscriptionId == null) {
                log.error("Could not extract subscription ID from event");
                return;
            }

            // Check if this is initial subscription or renewal
            UserSubscriptionPayment subscription = userSubscriptionPaymentRepository
                    .findByStripeSubscriptionId(subscriptionId)
                    .orElse(null);

            if (subscription == null) {
                // Initial subscription payment
                handleInitialSubscriptionPayment(invoice, subscriptionId);
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
            log.info("Processing invoice.payment_failed event");

            Invoice invoice;

            // Try to deserialize using EventDataObjectDeserializer first
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                invoice = (Invoice) event.getDataObjectDeserializer().getObject().get();
                log.info("Successfully deserialized Invoice using EventDataObjectDeserializer");
            } else {
                // Fallback: manually deserialize from JSON
                log.info("EventDataObjectDeserializer returned empty, using manual deserialization");
                invoice = ApiResource.GSON.fromJson(
                        event.getDataObjectDeserializer().getRawJson(),
                        Invoice.class);
            }

            if (invoice == null) {
                log.error("Invoice is still null after deserialization attempts");
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

    private void handleInitialSubscriptionPayment(Invoice invoice, String subscriptionId) {
        // Extract metadata directly from invoice line items (where Stripe stores
        // subscription metadata)
        Map<String, String> metadata = extractMetadataFromInvoice(invoice);

        if (!metadata.containsKey("transaction_id")) {
            log.warn("No transaction_id found in invoice metadata");
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

        // Get period dates from line items (correct dates)
        long periodStart = invoice.getPeriodStart();
        long periodEnd = invoice.getPeriodEnd();

        // Extract correct period from line items if available
        if (invoice.getLines() != null && !invoice.getLines().getData().isEmpty()) {
            var lineItem = invoice.getLines().getData().get(0);
            if (lineItem.getPeriod() != null) {
                periodStart = lineItem.getPeriod().getStart();
                periodEnd = lineItem.getPeriod().getEnd();
            }
        }

        // Create UserSubscriptionPayment record
        UserSubscriptionPayment subscription = UserSubscriptionPayment.builder()
                .transaction(transaction)
                .subscriptionPlan(new SubscriptionPlan()) // Will be set by JPA
                .userId(userId)
                .stripeSubscriptionId(subscriptionId)
                .stripeInvoiceId(invoice.getId())
                .firstPeriodStart(subscriptionService.convertTimestampToLocalDateTime(periodStart))
                .firstPeriodEnd(subscriptionService.convertTimestampToLocalDateTime(periodEnd))
                .afterRenewalEnd(subscriptionService.convertTimestampToLocalDateTime(periodEnd))
                .isAutoRenew(true)
                .status(UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
                .build();

        // Set subscription plan (simplified - in real implementation, fetch from
        // repository)
        subscription.getSubscriptionPlan().setId(subscriptionPlanId);

        userSubscriptionPaymentRepository.save(subscription);

        log.info("Subscription saved with subscription_id: {}, period: {} to {}",
                subscriptionId, periodStart, periodEnd);

        // Send subscription success email notification
        sendSubscriptionSuccessNotification(subscription, metadata, invoice);

        // Send user subscription message (set is_subscribed = true)
        sendUserSubscriptionMessage(userId, true, "SUBSCRIBED");
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
        Map<String, String> metadata = extractMetadataFromInvoice(invoice);

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

            // Send subscription failure email notification
            sendSubscriptionFailureNotification(userId, metadata, invoice);

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
                .retryCount(attemptCount)
                .status(Renewal.RenewalStatus.FAILED)
                .nextPaymentAttemptAt(nextAttempt)
                .build();

        renewalRepository.save(renewal);

        // Check if max retry count reached (3 attempts)
        final int MAX_RETRY_COUNT = 3;
        if (attemptCount >= MAX_RETRY_COUNT) {
            log.warn("Max retry count ({}) reached for subscription: {}, canceling subscription",
                    MAX_RETRY_COUNT, subscription.getStripeSubscriptionId());

            // Cancel subscription and set is_subscribed = false
            try {
                com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();
                subscription.setStatus(UserSubscriptionPayment.SubscriptionStatus.CANCELED);
                subscription.setCanceledAt(LocalDateTime.now());
                subscription.setIsAutoRenew(false);
                userSubscriptionPaymentRepository.save(subscription);

                // Send user subscription message (set is_subscribed = false)
                sendUserSubscriptionMessage(subscription.getUserId(), false, "CANCELED");

                log.info("Subscription canceled due to max retry failures: {}", subscription.getStripeSubscriptionId());
            } catch (Exception e) {
                log.error("Failed to cancel subscription after max retries: {}", e.getMessage());
            }
        }

        // Send retry failure notification
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("RENEWAL_FAILED")
                .retryCount(attemptCount)
                .build();

        messagePublisherService.sendSubscriptionNotificationMessage(message);
    }

    private Map<String, String> extractMetadataFromInvoice(Invoice invoice) {
        try {
            // First try to get metadata from invoice line items
            if (invoice.getLines() != null && invoice.getLines().getData() != null
                    && !invoice.getLines().getData().isEmpty()) {
                com.stripe.model.InvoiceLineItem lineItem = invoice.getLines().getData().get(0);
                if (lineItem.getMetadata() != null && !lineItem.getMetadata().isEmpty()) {
                    log.info("Found metadata in invoice line items: {}", lineItem.getMetadata());
                    return lineItem.getMetadata();
                }
            }

            // Fallback: try to get metadata from subscription
            if (invoice.getSubscription() != null) {
                return getSubscriptionMetadata(invoice.getSubscription());
            }

            log.warn("No metadata found in invoice line items or subscription");
            return Map.of();
        } catch (Exception e) {
            log.error("Error extracting metadata from invoice", e);
            return Map.of();
        }
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

    private String extractSubscriptionIdFromEvent(Event event) {
        try {
            // Parse the raw JSON to extract subscription ID from the nested structure
            String rawJson = event.getDataObjectDeserializer().getRawJson();
            log.info("Raw JSON for subscription extraction: {}", rawJson);

            com.google.gson.JsonObject jsonObject = com.google.gson.JsonParser.parseString(rawJson).getAsJsonObject();

            // From your webhook payload, check parent.subscription_details.subscription
            // first
            if (jsonObject.has("parent") && !jsonObject.get("parent").isJsonNull()) {
                com.google.gson.JsonObject parent = jsonObject.getAsJsonObject("parent");
                if (parent.has("subscription_details") && !parent.get("subscription_details").isJsonNull()) {
                    com.google.gson.JsonObject subscriptionDetails = parent.getAsJsonObject("subscription_details");
                    if (subscriptionDetails.has("subscription")
                            && !subscriptionDetails.get("subscription").isJsonNull()) {
                        String subscriptionId = subscriptionDetails.get("subscription").getAsString();
                        log.info("Found subscription ID in parent.subscription_details: {}", subscriptionId);
                        return subscriptionId;
                    }
                }
            }

            // Try direct subscription field
            if (jsonObject.has("subscription") && !jsonObject.get("subscription").isJsonNull()) {
                String subscriptionId = jsonObject.get("subscription").getAsString();
                log.info("Found subscription ID directly: {}", subscriptionId);
                return subscriptionId;
            }

            // Try from lines data (fallback)
            if (jsonObject.has("lines") && jsonObject.get("lines").isJsonObject()) {
                com.google.gson.JsonObject lines = jsonObject.getAsJsonObject("lines");
                if (lines.has("data") && lines.get("data").isJsonArray()) {
                    com.google.gson.JsonArray dataArray = lines.getAsJsonArray("data");
                    if (dataArray.size() > 0) {
                        com.google.gson.JsonObject firstItem = dataArray.get(0).getAsJsonObject();
                        if (firstItem.has("subscription") && !firstItem.get("subscription").isJsonNull()) {
                            String subscriptionId = firstItem.get("subscription").getAsString();
                            log.info("Found subscription ID in lines data: {}", subscriptionId);
                            return subscriptionId;
                        }
                    }
                }
            }

            log.warn(
                    "Could not find subscription ID in event JSON - checked parent.subscription_details, direct subscription field, and lines data");
            return null;
        } catch (Exception e) {
            log.error("Error extracting subscription ID from event", e);
            return null;
        }
    }

    private void sendUserSubscriptionMessage(UUID userId, boolean isSubscribed, String status) {
        try {
            UserSubscriptionMessage message = UserSubscriptionMessage.builder()
                    .userId(userId)
                    .isSubscribed(isSubscribed)
                    .status(status)
                    .build();

            messagePublisherService.sendUserSubscriptionMessage(message);
            log.info("User subscription message sent for user: {} with isSubscribed: {}", userId, isSubscribed);
        } catch (Exception e) {
            log.error("Failed to send user subscription message for user: {}", userId, e);
        }
    }

    private void sendSubscriptionSuccessNotification(UserSubscriptionPayment subscription, Map<String, String> metadata,
            Invoice invoice) {
        try {
            // Get subscription plan name
            SubscriptionPlan subscriptionPlan = subscriptionPlanRepository
                    .findById(UUID.fromString(metadata.get("subscription_plan_id")))
                    .orElse(null);

            String subscriptionName = subscriptionPlan != null ? subscriptionPlan.getName() : "Premium Subscription";

            // Get invoice PDF URL from Stripe
            String invoicePdfUrl = invoice.getHostedInvoiceUrl();

            PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                    .userId(subscription.getUserId())
                    .eventType("SUBSCRIPTION_SUCCESS")
                    .subscriptionName(subscriptionName)
                    .amount(subscription.getTransaction().getAmount().toString())
                    .currency(subscription.getTransaction().getCurrency().toUpperCase())
                    .invoicePdfUrl(invoicePdfUrl)
                    .build();

            messagePublisherService.sendPaymentNotificationMessage(notificationMessage);
            log.info("Subscription success notification sent for user: {} and subscription: {} with invoice: {}",
                    subscription.getUserId(), subscriptionName, invoicePdfUrl);
        } catch (Exception e) {
            log.error("Failed to send subscription success notification for user: {}",
                    subscription.getUserId(), e);
        }
    }

    private void sendSubscriptionFailureNotification(UUID userId, Map<String, String> metadata, Invoice invoice) {
        try {
            // Get subscription plan name if available
            String subscriptionName = "Premium Subscription";
            if (metadata.containsKey("subscription_plan_id")) {
                SubscriptionPlan subscriptionPlan = subscriptionPlanRepository
                        .findById(UUID.fromString(metadata.get("subscription_plan_id")))
                        .orElse(null);
                if (subscriptionPlan != null) {
                    subscriptionName = subscriptionPlan.getName();
                }
            }

            // Get amount and currency from invoice
            String amount = invoice.getAmountDue() != null ? String.valueOf(invoice.getAmountDue() / 100.0) : "0.00";
            String currency = invoice.getCurrency() != null ? invoice.getCurrency().toUpperCase() : "USD";

            PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                    .userId(userId)
                    .eventType("SUBSCRIPTION_FAILED")
                    .subscriptionName(subscriptionName)
                    .amount(amount)
                    .currency(currency)
                    .invoicePdfUrl(null) // No PDF for failed payments
                    .build();

            messagePublisherService.sendPaymentNotificationMessage(notificationMessage);
            log.info("Subscription failure notification sent for user: {} and subscription: {}",
                    userId, subscriptionName);
        } catch (Exception e) {
            log.error("Failed to send subscription failure notification for user: {}", userId, e);
        }
    }
}