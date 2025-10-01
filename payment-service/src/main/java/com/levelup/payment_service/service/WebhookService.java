package com.levelup.payment_service.service;

import com.google.gson.Gson;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.*;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final PaymentService paymentService;
    private final SubscriptionWebhookService subscriptionWebhookService;

    public void handleStripeWebhook(String payload, String sigHeader) {
        try {
            // log.info("this is payload: " + payload);
            // Verify webhook signature
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

            log.info("Event created successfully");

            log.info("Received Stripe webhook event: {}", event.getType());

            switch (event.getType()) {
                // Payment Intent events (for course purchases)
                case "payment_intent.succeeded":

                    handlePaymentIntentSucceeded(event);
                    break;
                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;

                // Subscription events
                case "invoice.payment_succeeded":
                    subscriptionWebhookService.handleInvoicePaymentSucceeded(event);
                    break;
                case "invoice.payment_failed":
                    subscriptionWebhookService.handleInvoicePaymentFailed(event);
                    break;

                default:
                    log.warn("Unhandled event type: {}", event.getType());
            }

        } catch (SignatureVerificationException e) {
            log.error("Invalid signature for webhook", e);
            throw new RuntimeException("Invalid webhook signature", e);
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            throw new RuntimeException("Error processing webhook", e);
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        try {
            log.info("Processing payment_intent.succeeded event");

            PaymentIntent paymentIntent;

            // Try to deserialize using EventDataObjectDeserializer first
            if (event.getDataObjectDeserializer().getObject().isPresent()) {
                paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().get();
                log.info("Successfully deserialized PaymentIntent using EventDataObjectDeserializer");
            } else {
                // Fallback: manually deserialize from JSON
                log.info("EventDataObjectDeserializer returned empty, using manual deserialization");
                paymentIntent = ApiResource.GSON.fromJson(
                        event.getDataObjectDeserializer().getRawJson(),
                        PaymentIntent.class);
            }

            if (paymentIntent == null) {
                log.error("PaymentIntent is still null after deserialization attempts");
                return;
            }

            Map<String, String> metadata = paymentIntent.getMetadata();
            log.info("Processing payment success for PaymentIntent: {} with metadata: {}",
                    paymentIntent.getId(), metadata);

            // CHECK: Skip subscription PaymentIntents (they have empty metadata or wrong
            // transaction_type)
            if (metadata == null || metadata.isEmpty()) {
                log.info("PaymentIntent {} has no metadata - this is a SUBSCRIPTION payment, skipping. " +
                        "Subscription payments are handled via invoice.payment_succeeded events.",
                        paymentIntent.getId());
                return;
            }

            // CHECK: Verify it's specifically a course purchase using transaction_type
            String transactionType = metadata.get("transaction_type");
            if (!"COURSE_PURCHASE".equals(transactionType)) {
                log.info("PaymentIntent {} has transaction_type '{}' - not a course purchase, skipping. " +
                        "Only COURSE_PURCHASE PaymentIntents are processed here.",
                        paymentIntent.getId(), transactionType);
                return;
            }

            // CHECK: Verify it has required course purchase metadata
            if (!metadata.containsKey("transaction_id") || !metadata.containsKey("user_id")) {
                log.info(
                        "PaymentIntent {} missing required course purchase metadata (transaction_id, user_id) - skipping",
                        paymentIntent.getId());
                return;
            }

            // Only process course purchases with proper metadata
            log.info("Processing COURSE PURCHASE payment for PaymentIntent: {}", paymentIntent.getId());
            paymentService.handlePaymentSuccess(paymentIntent.getId(), metadata);

        } catch (Exception e) {
            log.error("Error handling payment intent succeeded", e);
            throw new RuntimeException("Error handling payment success", e);
        }
    }

    private void handlePaymentIntentFailed(Event event) {
        try {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
            if (paymentIntent == null) {
                log.error("PaymentIntent is null in webhook event");
                return;
            }

            Map<String, String> metadata = paymentIntent.getMetadata();

            log.info("Processing payment failure for PaymentIntent: {}", paymentIntent.getId());

            paymentService.handlePaymentFailure(paymentIntent.getId(), metadata);

        } catch (Exception e) {
            log.error("Error handling payment intent failed", e);
            throw new RuntimeException("Error handling payment failure", e);
        }
    }
}