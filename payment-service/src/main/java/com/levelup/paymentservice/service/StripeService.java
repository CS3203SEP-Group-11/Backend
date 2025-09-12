package com.levelup.paymentservice.service;

import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.param.*;
import com.stripe.net.Webhook;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class StripeService {

    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);

    public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, UUID userId, String description)
            throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount((long) (amount.doubleValue() * 100)) // Convert to cents
                .setCurrency(currency.toLowerCase())
                .setDescription(description)
                .putMetadata("userId", userId.toString())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        logger.info("Created PaymentIntent: {} for user: {}", paymentIntent.getId(), userId);
        return paymentIntent;
    }

    public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntentConfirmParams params = PaymentIntentConfirmParams.builder().build();
        return paymentIntent.confirm(params);
    }

    public PaymentIntent cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntentCancelParams params = PaymentIntentCancelParams.builder().build();
        return paymentIntent.cancel(params);
    }

    public Customer createCustomer(UUID userId, String email, String name) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name)
                .putMetadata("userId", userId.toString())
                .build();

        Customer customer = Customer.create(params);
        logger.info("Created Stripe customer: {} for user: {}", customer.getId(), userId);
        return customer;
    }

    public Customer retrieveCustomer(String customerId) throws StripeException {
        return Customer.retrieve(customerId);
    }

    public Subscription createSubscription(String customerId, String priceId, Integer trialPeriodDays)
            throws StripeException {
        SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                .setCustomer(customerId)
                .addItem(
                        SubscriptionCreateParams.Item.builder()
                                .setPrice(priceId)
                                .build());

        if (trialPeriodDays != null && trialPeriodDays > 0) {
            paramsBuilder.setTrialPeriodDays((long) trialPeriodDays);
        }

        Subscription subscription = Subscription.create(paramsBuilder.build());
        logger.info("Created Stripe subscription: {} for customer: {}", subscription.getId(), customerId);
        return subscription;
    }

    public Subscription cancelSubscription(String subscriptionId, boolean immediately) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);

        if (immediately) {
            SubscriptionCancelParams params = SubscriptionCancelParams.builder()
                    .setProrate(true)
                    .build();
            return subscription.cancel(params);
        } else {
            SubscriptionUpdateParams params = SubscriptionUpdateParams.builder()
                    .setCancelAtPeriodEnd(true)
                    .build();
            return subscription.update(params);
        }
    }

    public Subscription updateSubscription(String subscriptionId, Map<String, Object> params) throws StripeException {
        Subscription subscription = Subscription.retrieve(subscriptionId);
        return subscription.update(params);
    }

    public Refund createRefund(String paymentIntentId, Long amount, String reason) throws StripeException {
        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setPaymentIntent(paymentIntentId);

        if (amount != null) {
            paramsBuilder.setAmount(amount);
        }

        if (reason != null) {
            paramsBuilder.setReason(RefundCreateParams.Reason.valueOf(reason.toUpperCase()));
        }

        Refund refund = Refund.create(paramsBuilder.build());
        logger.info("Created refund: {} for PaymentIntent: {}", refund.getId(), paymentIntentId);
        return refund;
    }

    public Price createPrice(BigDecimal amount, String currency, String interval, String productId)
            throws StripeException {
        PriceCreateParams params = PriceCreateParams.builder()
                .setUnitAmount((long) (amount.doubleValue() * 100))
                .setCurrency(currency.toLowerCase())
                .setRecurring(
                        PriceCreateParams.Recurring.builder()
                                .setInterval(PriceCreateParams.Recurring.Interval.valueOf(interval.toUpperCase()))
                                .build())
                .setProduct(productId)
                .build();

        return Price.create(params);
    }

    public Product createProduct(String name, String description) throws StripeException {
        ProductCreateParams params = ProductCreateParams.builder()
                .setName(name)
                .setDescription(description)
                .build();

        return Product.create(params);
    }

    public Transfer createTransfer(Long amount, String currency, String destinationAccount) throws StripeException {
        TransferCreateParams params = TransferCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency.toLowerCase())
                .setDestination(destinationAccount)
                .build();

        Transfer transfer = Transfer.create(params);
        logger.info("Created transfer: {} to account: {}", transfer.getId(), destinationAccount);
        return transfer;
    }

    public Event constructEvent(String payload, String sigHeader, String webhookSecret) throws StripeException {
        return Webhook.constructEvent(payload, sigHeader, webhookSecret);
    }
}
