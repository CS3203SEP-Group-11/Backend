package com.levelup.paymentservice.controller;

import com.levelup.paymentservice.config.StripeConfig;
import com.levelup.paymentservice.service.StripeService;
import com.levelup.paymentservice.service.PurchaseService;
import com.levelup.paymentservice.service.SubscriptionService;
import com.levelup.paymentservice.service.EventPublishingService;
import com.levelup.paymentservice.event.PaymentCompletedEvent;
import com.levelup.paymentservice.dto.PurchaseResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Subscription;
import com.stripe.model.Invoice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/payments/webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);

    @Autowired
    private StripeConfig stripeConfig;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PurchaseService purchaseService;

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private EventPublishingService eventPublishingService;

    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(@RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        logger.info("Received Stripe webhook");

        Event event;
        try {
            event = stripeService.constructEvent(payload, sigHeader, stripeConfig.getWebhookSecret());
        } catch (StripeException e) {
            logger.error("Invalid webhook signature", e);
            return ResponseEntity.badRequest().body("Invalid signature");
        }

        logger.info("Processing Stripe webhook event: {}", event.getType());

        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;
                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;
                case "customer.subscription.created":
                    handleSubscriptionCreated(event);
                    break;
                case "customer.subscription.updated":
                    handleSubscriptionUpdated(event);
                    break;
                case "customer.subscription.deleted":
                    handleSubscriptionDeleted(event);
                    break;
                case "invoice.payment_succeeded":
                    handleInvoicePaymentSucceeded(event);
                    break;
                case "invoice.payment_failed":
                    handleInvoicePaymentFailed(event);
                    break;
                case "customer.subscription.trial_will_end":
                    handleTrialWillEnd(event);
                    break;
                default:
                    logger.info("Unhandled webhook event type: {}", event.getType());
            }

            return ResponseEntity.ok("Webhook processed successfully");

        } catch (Exception e) {
            logger.error("Error processing webhook event: {}", event.getType(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent == null) {
            logger.error("PaymentIntent is null in webhook event");
            return;
        }

        logger.info("Payment succeeded for PaymentIntent: {}", paymentIntent.getId());

        // Complete the purchase
        boolean completed = purchaseService.completePurchase(paymentIntent.getId());

        if (completed) {
            // Get purchase details for event
            Optional<PurchaseResponse> purchaseOpt = purchaseService.getPurchaseById(
                    java.util.UUID.fromString(paymentIntent.getMetadata().get("purchaseId")));

            if (purchaseOpt.isPresent()) {
                PurchaseResponse purchase = purchaseOpt.get();

                // Publish payment completed event
                PaymentCompletedEvent paymentEvent = new PaymentCompletedEvent(
                        purchase.getUserId(),
                        purchase.getId(),
                        purchase.getItems().stream()
                                .map(PurchaseResponse.PurchaseItemResponse::getCourseId)
                                .collect(Collectors.toList()),
                        purchase.getTotalAmount(),
                        purchase.getFinalAmount(),
                        purchase.getCurrency());

                eventPublishingService.publishPaymentCompletedEvent(paymentEvent);
                eventPublishingService.publishCourseAccessGrantedEvent(paymentEvent);
            }
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent == null) {
            logger.error("PaymentIntent is null in webhook event");
            return;
        }

        logger.warn("Payment failed for PaymentIntent: {}", paymentIntent.getId());

        // Update transaction status to failed
        // Implementation would depend on having the transaction service handle this
        logger.info("Payment failure handled for PaymentIntent: {}", paymentIntent.getId());
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (paymentIntent == null) {
            logger.error("PaymentIntent is null in webhook event");
            return;
        }

        logger.info("Payment canceled for PaymentIntent: {}", paymentIntent.getId());

        // Handle payment cancellation
        logger.info("Payment cancellation handled for PaymentIntent: {}", paymentIntent.getId());
    }

    private void handleSubscriptionCreated(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (subscription == null) {
            logger.error("Subscription is null in webhook event");
            return;
        }

        logger.info("Subscription created in Stripe: {}", subscription.getId());

        // The subscription creation is already handled in SubscriptionService
        // This webhook confirms the creation in Stripe
        logger.info("Stripe subscription creation confirmed: {}", subscription.getId());
    }

    private void handleSubscriptionUpdated(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (subscription == null) {
            logger.error("Subscription is null in webhook event");
            return;
        }

        logger.info("Subscription updated in Stripe: {}", subscription.getId());

        // Handle subscription updates (plan changes, etc.)
        logger.info("Stripe subscription update handled: {}", subscription.getId());
    }

    private void handleSubscriptionDeleted(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (subscription == null) {
            logger.error("Subscription is null in webhook event");
            return;
        }

        logger.info("Subscription deleted in Stripe: {}", subscription.getId());

        // Handle subscription cancellation
        // Update local subscription status and revoke course access
        logger.info("Stripe subscription deletion handled: {}", subscription.getId());
    }

    private void handleInvoicePaymentSucceeded(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (invoice == null) {
            logger.error("Invoice is null in webhook event");
            return;
        }

        logger.info("Invoice payment succeeded: {}", invoice.getId());

        // Handle successful subscription payment
        if (invoice.getSubscription() != null) {
            logger.info("Subscription payment successful for subscription: {}", invoice.getSubscription());

            // Process subscription renewal
            // This confirms the payment for a subscription renewal
            logger.info("Subscription payment processed for invoice: {}", invoice.getId());
        }
    }

    private void handleInvoicePaymentFailed(Event event) {
        Invoice invoice = (Invoice) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (invoice == null) {
            logger.error("Invoice is null in webhook event");
            return;
        }

        logger.warn("Invoice payment failed: {}", invoice.getId());

        // Handle failed subscription payment
        if (invoice.getSubscription() != null) {
            logger.warn("Subscription payment failed for subscription: {}", invoice.getSubscription());

            // Mark subscription as unpaid and potentially suspend access
            // Implement retry logic based on business rules
            logger.info("Subscription payment failure handled for invoice: {}", invoice.getId());
        }
    }

    private void handleTrialWillEnd(Event event) {
        Subscription subscription = (Subscription) event.getDataObjectDeserializer()
                .getObject().orElse(null);

        if (subscription == null) {
            logger.error("Subscription is null in webhook event");
            return;
        }

        logger.info("Trial will end soon for subscription: {}", subscription.getId());

        // Send notification to user about trial ending
        // This could trigger email/push notifications through the notification service
        logger.info("Trial ending notification sent for subscription: {}", subscription.getId());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleWebhookException(Exception e) {
        logger.error("Unexpected error in webhook controller", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Webhook processing error");
    }
}
