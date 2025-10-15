package com.levelup.payment_service.service;

import com.levelup.payment_service.client.UserServiceClient;
import com.levelup.payment_service.dto.external.UserDto;
import com.levelup.payment_service.dto.message.PaymentNotificationMessage;
import com.levelup.payment_service.dto.message.SubscriptionMessage;
import com.levelup.payment_service.dto.message.UserSubscriptionMessage;
import com.levelup.payment_service.dto.request.CreateSubscriptionRequest;
import com.levelup.payment_service.dto.response.SubscriptionResponse;
import com.levelup.payment_service.dto.response.SubscriptionCancelResponse;
import com.levelup.payment_service.dto.response.SubscriptionRefundResponse;
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
                    .paymentIntentId(subscription.getLatestInvoice() != null
                            ? Invoice.retrieve(subscription.getLatestInvoice()).getPaymentIntent()
                            : null)
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
        metadata.put("transaction_type", "SUBSCRIPTION_PAYMENT");

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
    public SubscriptionCancelResponse cancelSubscription(UUID subscriptionId, UUID userId) {
        UserSubscriptionPayment subscription = null;
        try {
            log.info("Canceling subscription: {} for user: {}", subscriptionId, userId);

            // Find subscription and verify ownership
            subscription = userSubscriptionPaymentRepository
                    .findByIdAndUserId(subscriptionId, userId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            if (subscription.getStatus() != UserSubscriptionPayment.SubscriptionStatus.ACTIVE) {
                throw new RuntimeException("Subscription is not active");
            }

            // Cancel the subscription and capture the returned object
            com.stripe.model.Subscription canceledSubscription = com.stripe.model.Subscription
                    .retrieve(subscription.getStripeSubscriptionId()).cancel();

            log.info("Stripe cancellation successful. Status: {}, Canceled at: {}",
                    canceledSubscription.getStatus(), canceledSubscription.getCanceledAt());

            // ONLY UPDATE DATABASE IF STRIPE CANCELLATION SUCCEEDS
            subscription.setStatus(UserSubscriptionPayment.SubscriptionStatus.CANCELED);
            subscription.setCanceledAt(LocalDateTime.now());
            subscription.setIsAutoRenew(false);
            userSubscriptionPaymentRepository.save(subscription);

            // Send user subscription message (set is_subscribed = false)
            sendUserSubscriptionMessage(subscription.getUserId(), false, "CANCELED");

            // Send cancellation success email notification
            sendCancellationSuccessNotification(subscription);

            log.info("Subscription canceled successfully");

            return SubscriptionCancelResponse.builder()
                    .subscriptionId(subscriptionId)
                    .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                    .status("CANCELED")
                    .message("Subscription canceled successfully")
                    .canceledAt(subscription.getCanceledAt())
                    .success(true)
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while canceling subscription: {}", e.getMessage(), e);

            // Handle cancellation failure - send notification only, don't update database
            if (subscription != null) {
                sendCancelFailureNotification(subscription, e.getMessage());
            }

            return SubscriptionCancelResponse.builder()
                    .subscriptionId(subscriptionId)
                    .stripeSubscriptionId(subscription != null ? subscription.getStripeSubscriptionId() : null)
                    .status("CANCEL_FAILED")
                    .message("Failed to cancel subscription")
                    .failureReason(e.getMessage())
                    .success(false)
                    .build();

        } catch (Exception e) {
            log.error("Error canceling subscription: {}", e.getMessage(), e);

            // Handle general failure - send notification only, don't update database
            if (subscription != null) {
                sendCancelFailureNotification(subscription, e.getMessage());
            }

            return SubscriptionCancelResponse.builder()
                    .subscriptionId(subscriptionId)
                    .stripeSubscriptionId(subscription != null ? subscription.getStripeSubscriptionId() : null)
                    .status("CANCEL_FAILED")
                    .message("Failed to cancel subscription")
                    .failureReason(e.getMessage())
                    .success(false)
                    .build();
        }
    }

    @Transactional
    public SubscriptionRefundResponse refundSubscription(UUID subscriptionId, UUID userId) {
        UserSubscriptionPayment subscription = null;
        try {
            log.info("Processing refund for subscription: {} and user: {}", subscriptionId, userId);

            // Find subscription and verify ownership
            subscription = userSubscriptionPaymentRepository
                    .findByIdAndUserId(subscriptionId, userId)
                    .orElseThrow(() -> new RuntimeException("Subscription not found"));

            // Verify refund eligibility
            validateRefundEligibility(subscription);

            // Process refund in Stripe using invoice
            Invoice invoice = Invoice.retrieve(subscription.getStripeInvoiceId());
            com.stripe.model.Refund refund = null;

            if (invoice.getPaymentIntent() != null) {
                refund = com.stripe.model.Refund.create(Map.of(
                        "payment_intent", invoice.getPaymentIntent(),
                        "reason", "requested_by_customer"));

                log.info("Stripe refund successful with ID: {}", refund.getId());

                // ONLY UPDATE DATABASE IF STRIPE REFUND SUCCEEDS
                // Create refund transaction
                Transaction refundTransaction = Transaction.builder()
                        .type(Transaction.TransactionType.REFUND)
                        .amount(subscription.getTransaction().getAmount())
                        .currency(subscription.getTransaction().getCurrency())
                        .status(Transaction.TransactionStatus.SUCCESS)
                        .build();

                transactionRepository.save(refundTransaction);
                log.info("Refund transaction created with ID: {}", refundTransaction.getId());

                // Update subscription
                subscription.setStatus(UserSubscriptionPayment.SubscriptionStatus.REFUNDED);
                subscription.setStripeRefundId(refund.getId());
                subscription.setCanceledAt(LocalDateTime.now());
                subscription.setIsAutoRenew(false);
                userSubscriptionPaymentRepository.save(subscription);

                // Cancel subscription in Stripe
                com.stripe.model.Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();

                // Send user subscription message (set is_subscribed = false)
                sendUserSubscriptionMessage(subscription.getUserId(), false, "REFUNDED");

                // Send refund success email notification
                sendRefundSuccessNotification(subscription);

                log.info("Subscription refunded successfully");

                return SubscriptionRefundResponse.builder()
                        .subscriptionId(subscriptionId)
                        .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                        .stripeRefundId(refund.getId())
                        .status("REFUNDED")
                        .message("Subscription refunded successfully")
                        .refundedAt(subscription.getCanceledAt())
                        .success(true)
                        .build();

            } else {
                throw new RuntimeException("Cannot process refund: No payment intent found for invoice");
            }

        } catch (StripeException e) {
            log.error("Stripe error while processing refund: {}", e.getMessage(), e);

            // Handle refund failure - send notification only, don't update database
            if (subscription != null) {
                sendRefundFailureNotification(subscription, e.getMessage());
            }

            return SubscriptionRefundResponse.builder()
                    .subscriptionId(subscriptionId)
                    .stripeSubscriptionId(subscription != null ? subscription.getStripeSubscriptionId() : null)
                    .status("REFUND_FAILED")
                    .message("Failed to process refund")
                    .failureReason(e.getMessage())
                    .success(false)
                    .build();

        } catch (Exception e) {
            log.error("Error processing refund: {}", e.getMessage(), e);

            // Handle general failure - send notification only, don't update database
            if (subscription != null) {
                sendRefundFailureNotification(subscription, e.getMessage());
            }

            return SubscriptionRefundResponse.builder()
                    .subscriptionId(subscriptionId)
                    .stripeSubscriptionId(subscription != null ? subscription.getStripeSubscriptionId() : null)
                    .status("REFUND_FAILED")
                    .message("Failed to process refund")
                    .failureReason(e.getMessage())
                    .success(false)
                    .build();
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

    private void sendRefundFailureNotification(UserSubscriptionPayment subscription, String failureReason) {
        log.info("Sending refund failure notification for subscription: {} with reason: {}",
                subscription.getId(), failureReason);

        // Send email notification via PaymentNotificationMessage
        PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                .userId(subscription.getUserId())
                .eventType("REFUND_FAILED")
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .amount(subscription.getTransaction().getAmount().toString())
                .currency(subscription.getTransaction().getCurrency())
                .build();

        messagePublisherService.sendPaymentNotificationMessage(notificationMessage);

        // Also send SubscriptionMessage for backward compatibility (existing
        // notification system)
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("REFUND_FAILED")
                .failureReason(failureReason)
                .build();

        // Send ONLY to notification service (don't send to course service since refund
        // failed)
        messagePublisherService.sendSubscriptionNotificationMessage(message);

        log.info("Refund failure notification sent for user: {}", subscription.getUserId());
    }

    private void sendCancelFailureNotification(UserSubscriptionPayment subscription, String failureReason) {
        log.info("Sending cancel failure notification for subscription: {} with reason: {}",
                subscription.getId(), failureReason);

        // Send email notification via PaymentNotificationMessage
        PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                .userId(subscription.getUserId())
                .eventType("CANCELLATION_FAILED")
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .amount(subscription.getTransaction().getAmount().toString())
                .currency(subscription.getTransaction().getCurrency())
                .build();

        messagePublisherService.sendPaymentNotificationMessage(notificationMessage);

        // Also send SubscriptionMessage for backward compatibility (existing
        // notification system)
        SubscriptionMessage message = SubscriptionMessage.builder()
                .userId(subscription.getUserId())
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .status("CANCEL_FAILED")
                .failureReason(failureReason)
                .build();

        // Send ONLY to notification service (don't send to course service since
        // cancellation failed)
        messagePublisherService.sendSubscriptionNotificationMessage(message);

        log.info("Cancel failure notification sent for user: {}", subscription.getUserId());
    }

    private void sendRefundSuccessNotification(UserSubscriptionPayment subscription) {
        log.info("Sending refund success notification for subscription: {}", subscription.getId());

        PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                .userId(subscription.getUserId())
                .eventType("REFUND_SUCCESS")
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .amount(subscription.getTransaction().getAmount().toString())
                .currency(subscription.getTransaction().getCurrency())
                .build();

        messagePublisherService.sendPaymentNotificationMessage(notificationMessage);
        log.info("Refund success notification sent for user: {}", subscription.getUserId());
    }

    private void sendCancellationSuccessNotification(UserSubscriptionPayment subscription) {
        log.info("Sending cancellation success notification for subscription: {}", subscription.getId());

        PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                .userId(subscription.getUserId())
                .eventType("CANCELLATION_SUCCESS")
                .subscriptionName(subscription.getSubscriptionPlan().getName())
                .amount(subscription.getTransaction().getAmount().toString())
                .currency(subscription.getTransaction().getCurrency())
                .build();

        messagePublisherService.sendPaymentNotificationMessage(notificationMessage);
        log.info("Cancellation success notification sent for user: {}", subscription.getUserId());
    }

    public LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneOffset.UTC);
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

    public java.util.Map<String, Object> getSubscriptionAnalytics() {
        java.util.Map<String, Object> analytics = new java.util.HashMap<>();
        java.util.List<UserSubscriptionPayment> allSubscriptions = userSubscriptionPaymentRepository.findAll();
        java.util.List<com.levelup.payment_service.model.SubscriptionPlan> allPlans = subscriptionPlanRepository.findAll();

        // Total subscribers (active)
        long totalSubscribers = allSubscriptions.stream()
            .filter(sub -> sub.getStatus() == com.levelup.payment_service.model.UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
            .count();

        // New subscribers this month
        java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
        long newSubscribersThisMonth = allSubscriptions.stream()
            .filter(sub -> {
                java.time.LocalDateTime createdAt = sub.getCreatedAt();
                return createdAt != null &&
                    createdAt.isAfter(thirtyDaysAgo) &&
                    sub.getStatus() == com.levelup.payment_service.model.UserSubscriptionPayment.SubscriptionStatus.ACTIVE;
            })
            .count();

        // Monthly recurring revenue (active subscriptions in last 30 days)
        java.math.BigDecimal monthlyRecurringRevenue = allSubscriptions.stream()
            .filter(sub -> sub.getStatus() == com.levelup.payment_service.model.UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
            .filter(sub -> {
                java.time.LocalDateTime createdAt = sub.getCreatedAt();
                return createdAt != null && createdAt.isAfter(thirtyDaysAgo);
            })
            .map(sub -> sub.getTransaction().getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Annual recurring revenue (active subscriptions in last 365 days)
        java.time.LocalDateTime yearAgo = java.time.LocalDateTime.now().minusDays(365);
        java.math.BigDecimal annualRecurringRevenue = allSubscriptions.stream()
            .filter(sub -> sub.getStatus() == com.levelup.payment_service.model.UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
            .filter(sub -> {
                java.time.LocalDateTime createdAt = sub.getCreatedAt();
                return createdAt != null && createdAt.isAfter(yearAgo);
            })
            .map(sub -> sub.getTransaction().getAmount())
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        // Revenue growth (this month vs last month)
        java.time.LocalDateTime sixtyDaysAgo = java.time.LocalDateTime.now().minusDays(60);
        long subscribersLastMonth = allSubscriptions.stream()
            .filter(sub -> {
                java.time.LocalDateTime createdAt = sub.getCreatedAt();
                return createdAt != null &&
                    createdAt.isAfter(sixtyDaysAgo) &&
                    createdAt.isBefore(thirtyDaysAgo) &&
                    sub.getStatus() == com.levelup.payment_service.model.UserSubscriptionPayment.SubscriptionStatus.ACTIVE;
            })
            .count();
        double revenueGrowth = subscribersLastMonth > 0 ?
            ((double) (newSubscribersThisMonth - subscribersLastMonth) / subscribersLastMonth * 100) : 0;

        // Plan distribution
        java.util.List<java.util.Map<String, Object>> planDistribution = new java.util.ArrayList<>();
        for (com.levelup.payment_service.model.SubscriptionPlan plan : allPlans) {
            long planSubscribers = allSubscriptions.stream()
                .filter(sub -> sub.getSubscriptionPlan().getId().equals(plan.getId()))
                .filter(sub -> sub.getStatus() == com.levelup.payment_service.model.UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
                .count();
            double percentage = totalSubscribers > 0 ? (planSubscribers * 100.0 / totalSubscribers) : 0;
            java.util.Map<String, Object> planInfo = new java.util.HashMap<>();
            planInfo.put("planId", plan.getId());
            planInfo.put("planName", plan.getName());
            planInfo.put("subscribers", planSubscribers);
            planInfo.put("percentage", Math.round(percentage * 10) / 10.0);
            planDistribution.add(planInfo);
        }

        analytics.put("totalSubscribers", totalSubscribers);
        analytics.put("newSubscribersThisMonth", newSubscribersThisMonth);
        analytics.put("monthlyRecurringRevenue", monthlyRecurringRevenue.doubleValue());
        analytics.put("annualRecurringRevenue", annualRecurringRevenue.doubleValue());
        analytics.put("revenueGrowth", Math.round(revenueGrowth * 10) / 10.0);
        analytics.put("planDistribution", planDistribution);

        return analytics;
    }
  
    public SubscriptionResponse getUserSubscription(UUID currentUserId) {
        return userSubscriptionPaymentRepository
                .findByUserIdAndStatus(currentUserId, UserSubscriptionPayment.SubscriptionStatus.ACTIVE)
                .map(subscription -> SubscriptionResponse.builder()
                        .subscriptionId(subscription.getId().toString())
                        .planName(subscription.getSubscriptionPlan().getName())
                        .amount(subscription.getTransaction().getAmount())
                        .currency(subscription.getTransaction().getCurrency())
                        .build()
                )
                .orElse(null);
    }
}