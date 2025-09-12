package com.levelup.paymentservice.service;

import com.levelup.paymentservice.client.CourseServiceClient;
import com.levelup.paymentservice.dto.CourseDto;
import com.levelup.paymentservice.dto.CreateSubscriptionRequest;
import com.levelup.paymentservice.dto.SubscriptionResponse;
import com.levelup.paymentservice.model.*;
import com.levelup.paymentservice.repository.SubscriptionRepository;
import com.levelup.paymentservice.repository.TransactionRepository;
import com.levelup.paymentservice.repository.RenewalRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SubscriptionService {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionService.class);

    @Autowired
    private SubscriptionRepository subscriptionRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private RenewalRepository renewalRepository;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Transactional
    public Mono<SubscriptionResponse> createSubscription(CreateSubscriptionRequest request) {
        logger.info("Creating subscription for user: {} and course: {}", request.getUserId(), request.getCourseId());

        return courseServiceClient.getCourseById(request.getCourseId())
                .flatMap(course -> {
                    if (course.getId() == null || !course.getActive() || !course.getSubscriptionEnabled()) {
                        return Mono.error(new IllegalArgumentException("Course is not available for subscription"));
                    }

                    return checkExistingSubscription(request.getUserId(), request.getCourseId())
                            .flatMap(exists -> {
                                if (exists) {
                                    return Mono.error(new IllegalStateException(
                                            "User already has an active subscription for this course"));
                                }
                                return createSubscriptionWithCourse(request, course);
                            });
                });
    }

    private Mono<Boolean> checkExistingSubscription(UUID userId, UUID courseId) {
        return Mono.fromCallable(() -> {
            Optional<Subscription> existing = subscriptionRepository
                    .findByUserIdAndCourseIdAndStatus(userId, courseId, Subscription.SubscriptionStatus.ACTIVE);
            return existing.isPresent();
        });
    }

    private Mono<SubscriptionResponse> createSubscriptionWithCourse(CreateSubscriptionRequest request,
            CourseDto course) {
        return Mono.fromCallable(() -> {
            BigDecimal amount = course.getSubscriptionPrice() != null ? course.getSubscriptionPrice()
                    : course.getPrice();

            // Create subscription entity
            Subscription subscription = new Subscription(
                    request.getUserId(),
                    request.getCourseId(),
                    course.getTitle(),
                    course.getInstructorId(),
                    request.getPlanType(),
                    amount);

            // Set trial if provided
            if (request.getTrialDays() != null && request.getTrialDays() > 0) {
                subscription.setTrialStart(LocalDateTime.now());
                subscription.setTrialEnd(LocalDateTime.now().plusDays(request.getTrialDays()));
            }

            Subscription savedSubscription = subscriptionRepository.save(subscription);

            try {
                // Create Stripe customer if needed
                Customer stripeCustomer = stripeService.createCustomer(
                        request.getUserId(),
                        "user-" + request.getUserId() + "@example.com", // This should come from user service
                        "User " + request.getUserId() // This should come from user service
                );

                savedSubscription.setStripeCustomerId(stripeCustomer.getId());

                // Create Stripe subscription
                com.stripe.model.Subscription stripeSubscription = stripeService.createSubscription(
                        stripeCustomer.getId(),
                        "price_placeholder", // This should be created dynamically or retrieved
                        request.getTrialDays());

                savedSubscription.setStripeSubscriptionId(stripeSubscription.getId());
                final Subscription finalSubscription = subscriptionRepository.save(savedSubscription);

                // Create initial transaction
                Transaction transaction = new Transaction(
                        request.getUserId(),
                        Transaction.TransactionType.SUBSCRIPTION,
                        amount,
                        "SUBSCRIPTION",
                        finalSubscription.getId());
                transactionRepository.save(transaction);

                // Grant course access
                courseServiceClient.grantCourseAccess(request.getUserId(), request.getCourseId())
                        .subscribe(
                                success -> logger.info("Course access granted for subscription: {}",
                                        finalSubscription.getId()),
                                error -> logger.error("Failed to grant course access for subscription: {}",
                                        finalSubscription.getId(), error));

                return convertToSubscriptionResponse(finalSubscription);

            } catch (StripeException e) {
                logger.error("Failed to create Stripe subscription", e);
                throw new RuntimeException("Failed to create subscription: " + e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public Optional<SubscriptionResponse> getSubscriptionById(UUID subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
                .map(this::convertToSubscriptionResponse);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getSubscriptionsByUserId(UUID userId) {
        return subscriptionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::convertToSubscriptionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean cancelSubscription(UUID subscriptionId, boolean immediately) {
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
        if (subscriptionOpt.isEmpty()) {
            return false;
        }

        Subscription subscription = subscriptionOpt.get();
        if (subscription.getStatus() != Subscription.SubscriptionStatus.ACTIVE) {
            logger.warn("Cannot cancel subscription with status: {}", subscription.getStatus());
            return false;
        }

        try {
            // Cancel in Stripe
            if (subscription.getStripeSubscriptionId() != null) {
                stripeService.cancelSubscription(subscription.getStripeSubscriptionId(), immediately);
            }

            if (immediately) {
                subscription.setStatus(Subscription.SubscriptionStatus.CANCELLED);
                subscription.setCancelledAt(LocalDateTime.now());

                // Revoke course access
                courseServiceClient.revokeCourseAccess(subscription.getUserId(), subscription.getCourseId())
                        .subscribe(
                                success -> logger.info("Course access revoked for cancelled subscription: {}",
                                        subscriptionId),
                                error -> logger.error("Failed to revoke course access for subscription: {}",
                                        subscriptionId, error));
            } else {
                // Will cancel at period end
                logger.info("Subscription {} will be cancelled at period end", subscriptionId);
            }

            subscriptionRepository.save(subscription);
            logger.info("Subscription cancelled: {}", subscriptionId);
            return true;

        } catch (StripeException e) {
            logger.error("Failed to cancel Stripe subscription: {}", subscription.getStripeSubscriptionId(), e);
            return false;
        }
    }

    @Transactional
    public void processRenewals() {
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> subscriptionsToRenew = subscriptionRepository
                .findSubscriptionsReadyForRenewal(now.plusDays(1)); // 1 day buffer

        logger.info("Processing {} subscription renewals", subscriptionsToRenew.size());

        for (Subscription subscription : subscriptionsToRenew) {
            try {
                processSubscriptionRenewal(subscription);
            } catch (Exception e) {
                logger.error("Failed to process renewal for subscription: {}", subscription.getId(), e);
            }
        }
    }

    private void processSubscriptionRenewal(Subscription subscription) {
        // Create renewal record
        Renewal renewal = new Renewal(
                subscription,
                subscription.getCurrentPeriodEnd(),
                subscription.getCurrentPeriodEnd(),
                subscription.getCurrentPeriodEnd().plusMonths(
                        subscription.getPlanType() == Subscription.PlanType.MONTHLY ? 1 : 12));

        renewalRepository.save(renewal);

        try {
            // The actual billing will be handled by Stripe webhooks
            // This just creates the renewal record for tracking
            subscription.renewSubscription();
            subscriptionRepository.save(subscription);

            renewal.setStatus(Renewal.RenewalStatus.COMPLETED);
            renewal.setProcessedAt(LocalDateTime.now());
            renewalRepository.save(renewal);

            logger.info("Subscription renewed successfully: {}", subscription.getId());

        } catch (Exception e) {
            renewal.markFailed("Renewal processing failed: " + e.getMessage());
            renewalRepository.save(renewal);
            throw e;
        }
    }

    private SubscriptionResponse convertToSubscriptionResponse(Subscription subscription) {
        SubscriptionResponse response = new SubscriptionResponse();
        response.setId(subscription.getId());
        response.setUserId(subscription.getUserId());
        response.setCourseId(subscription.getCourseId());
        response.setCourseTitle(subscription.getCourseTitle());
        response.setInstructorId(subscription.getInstructorId());
        response.setPlanType(subscription.getPlanType().name());
        response.setAmount(subscription.getAmount());
        response.setCurrency(subscription.getCurrency());
        response.setStatus(subscription.getStatus().name());
        response.setStripeSubscriptionId(subscription.getStripeSubscriptionId());
        response.setStripeCustomerId(subscription.getStripeCustomerId());
        response.setCurrentPeriodStart(subscription.getCurrentPeriodStart());
        response.setCurrentPeriodEnd(subscription.getCurrentPeriodEnd());
        response.setTrialStart(subscription.getTrialStart());
        response.setTrialEnd(subscription.getTrialEnd());
        response.setCancelledAt(subscription.getCancelledAt());
        response.setCreatedAt(subscription.getCreatedAt());
        return response;
    }
}
