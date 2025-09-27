package com.levelup.payment_service.service;

import com.levelup.payment_service.client.UserServiceClient;
import com.levelup.payment_service.dto.external.UserDto;
import com.levelup.payment_service.dto.message.SubscriptionMessage;
import com.levelup.payment_service.dto.request.CreateSubscriptionRequest;
import com.levelup.payment_service.dto.response.SubscriptionResponse;
import com.levelup.payment_service.model.SubscriptionPlan;
import com.levelup.payment_service.model.Transaction;
import com.levelup.payment_service.model.UserSubscriptionPayment;
import com.levelup.payment_service.repository.SubscriptionPlanRepository;
import com.levelup.payment_service.repository.TransactionRepository;
import com.levelup.payment_service.repository.UserSubscriptionPaymentRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionPaymentRepository userSubscriptionPaymentRepository;
    private final TransactionRepository transactionRepository;
    private final UserServiceClient userServiceClient;
    private final MessagePublisherService messagePublisherService;

    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request, UUID userId) {
        try {
            log.info("Creating subscription for user: {} with plan: {}", userId, request.getSubscriptionPlanId());

            // Step 1: Get subscription plan
            SubscriptionPlan plan = subscriptionPlanRepository
                    .findByIdAndIsActive(request.getSubscriptionPlanId(), true)
                    .orElseThrow(() -> new RuntimeException("Subscription plan not found or inactive"));

            // Step 2: Get user details
            UserDto user = userServiceClient.getUserById(userId);
            if (user == null) {
                throw new RuntimeException("User not found");
            }

            // Step 3: Get or create Stripe customer
            String stripeCustomerId = getOrCreateStripeCustomer(user);

            // Step 4: Create transaction record (PENDING status)
            Transaction transaction = Transaction.builder()
                    .type(Transaction.TransactionType.USER_SUBSCRIPTION_PAYMENT)
                    .amount(plan.getAmount())
                    .currency("usd")
                    .status(Transaction.TransactionStatus.PENDING)
                    .build();

            transaction = transactionRepository.save(transaction);
            log.info("Transaction created with ID: {}", transaction.getId());

            // Step 5: Create Stripe subscription
            Subscription subscription = createStripeSubscription(stripeCustomerId, plan, userId, transaction.getId());

            // Step 6: Return response with client secret
            String clientSecret = null;
            if (subscription.getLatestInvoice() != null) {
                Invoice invoice = Invoice.retrieve(subscription.getLatestInvoice());
                if (invoice.getPaymentIntent() != null) {
                    com.stripe.model.PaymentIntent paymentIntent = com.stripe.model.PaymentIntent
                            .retrieve(invoice.getPaymentIntent());
                    clientSecret = paymentIntent.getClientSecret();
                }
            }

            return SubscriptionResponse.builder()
                    .clientSecret(clientSecret)
                    .paymentIntentId(subscription.getLatestInvoice() != null ?
                            Invoice.retrieve(subscription.getLatestInvoice()).getPaymentIntent() : null)
                    .subscriptionId(subscription.getId())
                    .planName(plan.getName())
                    .amount(plan.getAmount())
                    .currency("usd")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating subscription", e);
            throw new RuntimeException("Failed to create subscription", e);
        } catch (Exception e) {
            log.error("Error creating subscription", e);
            throw new RuntimeException("Failed to create subscription", e);
        }
    }

    private String getOrCreateStripeCustomer(UserDto user) throws StripeException {
        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isEmpty()) {
            return user.getStripeCustomerId();
        }

        // Create new Stripe customer
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", user.getId().toString());

        Customer customer = Customer.create(Map.of(
                "email", user.getEmail(),
                "name", user.getFirstName() + " " + user.getLastName(),
                "metadata", metadata));

        // Update user with Stripe customer ID
        userServiceClient.updateUserStripeCustomerId(user.getId(), customer.getId());

        return customer.getId();
    }

    private Subscription createStripeSubscription(String customerId, SubscriptionPlan plan, UUID userId,
            UUID transactionId) throws StripeException {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", userId.toString());
        metadata.put("transaction_id", transactionId.toString());
        metadata.put("subscription_plan_id", plan.getId().toString());

        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(plan.getStripePriceId())
                        .build())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addExpand("latest_invoice.payment_intent")
                .putAllMetadata(metadata)
                .build();

        return Subscription.create(params);
    }

    @Transactional
    public void cancelSubscription(UUID subscriptionId, UUID userId) {
        try {
            log.info("Canceling subscription: {} for user: {}", subscriptionId, userId);

            // Find subscription and verify ownership
            UserSubscriptionPayment subscription = userSubscriptionPaymentRepository
                    .findByIdAndUserId(subscriptionId, userId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            if (subscription.getStatus() != UserSubscriptionPayment.SubscriptionStatus.ACTIVE) {
                throw new RuntimeException("Subscription is not active");
            }

            // Cancel in Stripe
            com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();

            // Update database
            subscription.setStatus(UserSubscriptionPayment.SubscriptionStatus.CANCELED);
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setIsAutoRenew(false);
            userSubscriptionPaymentRepository.save(subscription);

            // Send messages
            sendSubscriptionCancelMessages(subscription);

            log.info("Subscription canceled successfully");

        } catch (StripeException e) {
            log.error("Stripe error while canceling subscription", e);
            throw new RuntimeException("Failed to cancel subscription", e);
        } catch (Exception e) {
            log.error("Error canceling subscription", e);
            throw new RuntimeException("Failed to cancel subscription", e);
        }
    }

    @Transactional
    public void refundSubscription(UUID subscriptionId, UUID userId) {
        try {
            log.info("Processing refund for subscription: {} and user: {}", subscriptionId, userId);

            // Find subscription and verify ownership
            UserSubscriptionPayment subscription = userSubscriptionPaymentRepository
                    .findByIdAndUserId(subscriptionId, userId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            // Verify refund eligibility
            validateRefundEligibility(subscription);

            // Process refund in Stripe
            com.stripe.model.Refund refund = com.stripe.model.Refund.create(Map.of(
                    "payment_intent", subscription.getStripePaymentIntentId(),
                    "reason", "requested_by_customer"));

            // Create refund transaction
            Transaction refundTransaction = Transaction.builder()
                    .type(Transaction.TransactionType.REFUND)
                    .amount(subscription.getTransaction().getAmount())
                    .currency(subscription.getTransaction().getCurrency())
                    .status(Transaction.TransactionStatus.SUCCESS)
                    .build();

            transactionRepository.save(refundTransaction);

            // Update subscription
            subscription.setStatus(UserSubscriptionPayment.SubscriptionStatus.REFUNDED);
            subscription.setStripeRefundId(refund.getId());
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setIsAutoRenew(false);
            userSubscriptionPaymentRepository.save(subscription);

            // Cancel subscription in Stripe
            com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();

            // Send messages
            sendSubscriptionRefundMessages(subscription);

            log.info("Subscription refunded successfully");

        } catch (StripeException e) {
            log.error("Stripe error while processing refund", e);
            throw new RuntimeException("Failed to process refund", e);
        } catch (Exception e) {
            log.error("Error processing refund", e);
            throw new RuntimeException("Failed to process refund", e);
        }
    }

    private void validateRefundEligibility(UserSubscriptionPayment subscription) {
        // Check if within 14 days
        LocalDateTime fourteenDaysAgo = LocalDateTime.now().minusDays(14);
        if (subscription.getFirstPeriodStart().isBefore(fourteenDaysAgo)) {
            throw new RuntimeException("Refund period has expired (14 days)");
        }

        // Check if subscription is active
        if (subscription.getStatus() != UserSubscriptionPayment.SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Only active subscriptions can be refunded");
        }

        // Additional validation could be added here for course completion/certificates
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

    private void sendSubscriptionRefundMessages(UserSubscriptionPayment subscription) {
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("REFUNDED")
                .cancelDate(subscription.getCanceledAt())
                .build();

        // Send to course service for access removal
        messagePublisherService.sendSubscriptionMessage(message);

        // Send to notification service
        messagePublisherService.sendSubscriptionNotificationMessage(message);
    }

    public LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
    }
}