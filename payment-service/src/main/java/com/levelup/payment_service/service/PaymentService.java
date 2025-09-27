package com.levelup.payment_service.service;

import com.levelup.payment_service.client.CourseServiceClient;
import com.levelup.payment_service.dto.external.CourseDto;
import com.levelup.payment_service.dto.external.CourseServiceRequest;
import com.levelup.payment_service.dto.external.CourseServiceResponse;
import com.levelup.payment_service.dto.message.CourseEnrollmentMessage;
import com.levelup.payment_service.dto.message.PaymentNotificationMessage;
import com.levelup.payment_service.dto.request.CoursePurchaseRequest;
import com.levelup.payment_service.dto.response.PaymentIntentResponse;
import com.levelup.payment_service.model.PendingPurchaseItem;
import com.levelup.payment_service.model.PurchaseItem;
import com.levelup.payment_service.model.Transaction;
import com.levelup.payment_service.model.UserPurchase;
import com.levelup.payment_service.repository.PendingPurchaseItemRepository;
import com.levelup.payment_service.repository.PurchaseItemRepository;
import com.levelup.payment_service.repository.TransactionRepository;
import com.levelup.payment_service.repository.UserPurchaseRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.levelup.payment_service.dto.response.PaymentIntentResponse.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final TransactionRepository transactionRepository;
    private final UserPurchaseRepository userPurchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;
    private final PendingPurchaseItemRepository pendingPurchaseItemRepository;
    private final CourseServiceClient courseServiceClient;
    private final MessagePublisherService messagePublisherService;

    @Transactional
    public PaymentIntentResponse createCoursePurchasePayment(CoursePurchaseRequest request, UUID userId) {
        try {
            log.info("Creating course purchase payment for user: {} with courses: {}", userId, request.getCourseIds());

            // Step 1: Fetch course details from Course Service
            CourseServiceRequest courseRequest = new CourseServiceRequest(request.getCourseIds());
            CourseServiceResponse courseResponse = courseServiceClient.getCourseDetails(courseRequest);

            if (courseResponse.getCourses() == null || courseResponse.getCourses().isEmpty()) {
                throw new RuntimeException("No valid courses found");
            }

            // Step 2: Calculate total amount
            BigDecimal totalAmount = courseResponse.getCourses().stream()
                    .map(CourseDto::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("Total amount calculated: {}", totalAmount);

            // Step 3: Create transaction record
            Transaction transaction = Transaction.builder()
                    .type(Transaction.TransactionType.PURCHASE)
                    .amount(totalAmount)
                    .currency("usd")
                    .status(Transaction.TransactionStatus.PENDING)
                    .build();

            transaction = transactionRepository.save(transaction);
            log.info("Transaction created with ID: {}", transaction.getId());

            // Step 4: Create Stripe PaymentIntent
            PaymentIntent paymentIntent = createStripePaymentIntent(totalAmount, userId, transaction.getId());

            // Step 5: Create user purchase record
            UserPurchase userPurchase = UserPurchase.builder()
                    .transaction(transaction)
                    .stripePaymentIntentId(paymentIntent.getId())
                    .userId(userId)
                    .purchaseItems(new ArrayList<>())
                    .build();

            userPurchase = userPurchaseRepository.save(userPurchase);
            log.info("UserPurchase created with ID: {}", userPurchase.getId());

            // Step 6: Create pending purchase items for webhook processing
            List<PendingPurchaseItem> pendingItems = courseResponse.getCourses().stream()
                    .map(course -> PendingPurchaseItem.builder()
                            .stripePaymentIntentId(paymentIntent.getId())
                            .courseId(course.getId())
                            .courseName(course.getTitle())
                            .coursePrice(course.getPrice())
                            .build())
                    .collect(Collectors.toList());

            pendingPurchaseItemRepository.saveAll(pendingItems);
            log.info("Pending purchase items created for {} courses", pendingItems.size());

            // Step 7: Return payment intent response
            return builder()
                    .clientSecret(paymentIntent.getClientSecret())
                    .stripePaymentIntentId(paymentIntent.getId())
                    .amount(totalAmount)
                    .currency("usd")
                    .build();

        } catch (StripeException e) {
            log.error("Stripe error while creating payment intent", e);
            throw new RuntimeException("Failed to create payment intent", e);
        } catch (Exception e) {
            log.error("Error creating course purchase payment", e);
            throw new RuntimeException("Failed to create course purchase payment", e);
        }
    }

    private PaymentIntent createStripePaymentIntent(BigDecimal amount, UUID userId, UUID transactionId)
            throws StripeException {
        // Convert amount to cents (Stripe uses smallest currency unit)
        long amountInCents = amount.multiply(new BigDecimal("100")).longValue();

        Map<String, String> metadata = new HashMap<>();
        metadata.put("user_id", userId.toString());
        metadata.put("transaction_id", transactionId.toString());

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amountInCents)
                .setCurrency("usd")
                .putAllMetadata(metadata)
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        return PaymentIntent.create(params);
    }

    @Transactional
    public void handlePaymentSuccess(String paymentIntentId, Map<String, String> metadata) {
        try {
            log.info("Handling payment success for payment intent: {}", paymentIntentId);

            UUID transactionId = UUID.fromString(metadata.get("transaction_id"));
            UUID userId = UUID.fromString(metadata.get("user_id"));


            // Step 1: Update transaction status
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

            transaction.setStatus(Transaction.TransactionStatus.SUCCESS);
            log.info("transaction status set to SUCCESS for ID: {}", transactionId);
            log.info(transaction.getStatus().toString());
            transactionRepository.saveAndFlush(transaction);



            // Step 2: Update user purchase
            UserPurchase userPurchase = userPurchaseRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException(
                            "UserPurchase not found for payment intent: " + paymentIntentId));

            userPurchase.setPurchasedAt(LocalDateTime.now());
            UserPurchase savedUserPurchase = userPurchaseRepository.save(userPurchase);

            // Step 3: Get pending purchase items and convert to actual purchase items
            List<PendingPurchaseItem> pendingItems = pendingPurchaseItemRepository
                    .findByStripePaymentIntentId(paymentIntentId);

            if (pendingItems.isEmpty()) {
                log.warn("No pending purchase items found for payment intent: {}", paymentIntentId);
                return;
            }

            // Step 4: Create actual purchase items
            List<PurchaseItem> purchaseItems = pendingItems.stream()
                    .map(pending -> PurchaseItem.builder()
                            .userPurchase(savedUserPurchase)
                            .courseId(pending.getCourseId())
                            .courseName(pending.getCourseName())
                            .build())
                    .collect(Collectors.toList());

            purchaseItemRepository.saveAll(purchaseItems);
            log.info("Purchase items created for {} courses", purchaseItems.size());

            // Step 5: Send course enrollment message
            List<UUID> courseIds = pendingItems.stream()
                    .map(PendingPurchaseItem::getCourseId)
                    .collect(Collectors.toList());

            CourseEnrollmentMessage enrollmentMessage = new CourseEnrollmentMessage(userId, courseIds);
            messagePublisherService.sendCourseEnrollmentMessage(enrollmentMessage);

            // Step 6: Send payment notification
            List<String> courseNames = pendingItems.stream()
                    .map(PendingPurchaseItem::getCourseName)
                    .collect(Collectors.toList());

            PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                    .userId(userId)
                    .eventType("PURCHASE_SUCCESS")
                    .courseNames(courseNames)
                    .amount(transaction.getAmount().toString())
                    .currency(transaction.getCurrency())
                    .build();

            messagePublisherService.sendPaymentNotificationMessage(notificationMessage);

            // Step 7: Clean up pending items
            pendingPurchaseItemRepository.deleteByStripePaymentIntentId(paymentIntentId);

            log.info("Payment success handled successfully for transaction: {}", transactionId);

        } catch (Exception e) {
            log.error("Error handling payment success", e);
            throw new RuntimeException("Failed to handle payment success", e);
        }
    }

    @Transactional
    public void handlePaymentFailure(String paymentIntentId, Map<String, String> metadata) {
        try {
            log.info("Handling payment failure for payment intent: {}", paymentIntentId);

            UUID transactionId = UUID.fromString(metadata.get("transaction_id"));
            UUID userId = UUID.fromString(metadata.get("user_id"));

            // Step 1: Update transaction status
            Transaction transaction = transactionRepository.findById(transactionId)
                    .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

            transaction.setStatus(Transaction.TransactionStatus.FAILED);
            transactionRepository.save(transaction);

            // Step 2: Send failure notification
            PaymentNotificationMessage notificationMessage = PaymentNotificationMessage.builder()
                    .userId(userId)
                    .eventType("PURCHASE_FAILED")
                    .amount(transaction.getAmount().toString())
                    .currency(transaction.getCurrency())
                    .build();

            messagePublisherService.sendPaymentNotificationMessage(notificationMessage);

            // Step 3: Clean up pending items
            pendingPurchaseItemRepository.deleteByStripePaymentIntentId(paymentIntentId);

            log.info("Payment failure handled for transaction: {}", transactionId);

        } catch (Exception e) {
            log.error("Error handling payment failure", e);
            throw new RuntimeException("Failed to handle payment failure", e);
        }
    }
}