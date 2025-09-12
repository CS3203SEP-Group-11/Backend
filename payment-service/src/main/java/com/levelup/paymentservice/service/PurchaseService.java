package com.levelup.paymentservice.service;

import com.levelup.paymentservice.client.CourseServiceClient;
import com.levelup.paymentservice.dto.CourseDto;
import com.levelup.paymentservice.dto.CreatePurchaseRequest;
import com.levelup.paymentservice.dto.PurchaseResponse;
import com.levelup.paymentservice.model.*;
import com.levelup.paymentservice.repository.PurchaseRepository;
import com.levelup.paymentservice.repository.TransactionRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseService {

    private static final Logger logger = LoggerFactory.getLogger(PurchaseService.class);

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private StripeService stripeService;

    @Autowired
    private CourseServiceClient courseServiceClient;

    @Transactional
    public Mono<PurchaseResponse> createPurchase(CreatePurchaseRequest request) {
        logger.info("Creating purchase for user: {} with courses: {}", request.getUserId(), request.getCourseIds());

        return validateAndGetCourses(request.getCourseIds())
                .collectList()
                .flatMap(courses -> {
                    if (courses.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("No valid courses found"));
                    }

                    return createPurchaseWithCourses(request, courses);
                });
    }

    private Flux<CourseDto> validateAndGetCourses(List<UUID> courseIds) {
        return courseServiceClient.getCoursesByIds(courseIds)
                .filter(course -> course.getActive() != null && course.getActive())
                .filter(course -> course.getPrice() != null && course.getPrice().compareTo(BigDecimal.ZERO) > 0);
    }

    private Mono<PurchaseResponse> createPurchaseWithCourses(CreatePurchaseRequest request, List<CourseDto> courses) {
        return Mono.fromCallable(() -> {
            // Calculate totals
            BigDecimal totalAmount = courses.stream()
                    .map(CourseDto::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal discountAmount = request.getDiscountAmount() != null ? request.getDiscountAmount()
                    : BigDecimal.ZERO;
            BigDecimal finalAmount = totalAmount.subtract(discountAmount);

            // Create purchase entity
            Purchase purchase = new Purchase(request.getUserId(), totalAmount);
            purchase.setDiscountAmount(discountAmount);
            purchase.setFinalAmount(finalAmount);

            // Create purchase items
            List<PurchaseItem> items = courses.stream()
                    .map(course -> new PurchaseItem(
                            purchase,
                            course.getId(),
                            course.getTitle(),
                            course.getInstructorId(),
                            course.getInstructorName(),
                            course.getPrice()))
                    .collect(Collectors.toList());

            purchase.setItems(items);

            // Save purchase
            Purchase savedPurchase = purchaseRepository.save(purchase);

            try {
                // Create Stripe PaymentIntent
                PaymentIntent paymentIntent = stripeService.createPaymentIntent(
                        finalAmount,
                        "USD",
                        request.getUserId(),
                        "Course purchase: " + courses.stream()
                                .map(CourseDto::getTitle)
                                .collect(Collectors.joining(", ")));

                savedPurchase.setStripePaymentIntentId(paymentIntent.getId());
                savedPurchase = purchaseRepository.save(savedPurchase);

                // Create transaction record
                Transaction transaction = new Transaction(
                        request.getUserId(),
                        Transaction.TransactionType.PURCHASE,
                        finalAmount,
                        "PURCHASE",
                        savedPurchase.getId());
                transaction.setStripePaymentIntentId(paymentIntent.getId());
                transactionRepository.save(transaction);

                // Convert to response
                return convertToPurchaseResponse(savedPurchase, paymentIntent.getClientSecret());

            } catch (StripeException e) {
                logger.error("Failed to create Stripe PaymentIntent", e);
                throw new RuntimeException("Failed to create payment intent: " + e.getMessage());
            }
        });
    }

    @Transactional(readOnly = true)
    public Optional<PurchaseResponse> getPurchaseById(UUID purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .map(purchase -> convertToPurchaseResponse(purchase, null));
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponse> getPurchasesByUserId(UUID userId) {
        return purchaseRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(purchase -> convertToPurchaseResponse(purchase, null))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean completePurchase(String stripePaymentIntentId) {
        Optional<Purchase> purchaseOpt = purchaseRepository.findByStripePaymentIntentId(stripePaymentIntentId);
        if (purchaseOpt.isEmpty()) {
            logger.warn("No purchase found for PaymentIntent: {}", stripePaymentIntentId);
            return false;
        }

        Purchase purchase = purchaseOpt.get();
        purchase.setStatus(Purchase.PurchaseStatus.COMPLETED);
        purchaseRepository.save(purchase);

        // Update transaction status
        Optional<Transaction> transactionOpt = transactionRepository.findByStripePaymentIntentId(stripePaymentIntentId);
        if (transactionOpt.isPresent()) {
            Transaction transaction = transactionOpt.get();
            transaction.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(transaction);
        }

        // Grant course access (async)
        List<UUID> courseIds = purchase.getItems().stream()
                .map(PurchaseItem::getCourseId)
                .collect(Collectors.toList());

        courseServiceClient.grantBulkCourseAccess(purchase.getUserId(), courseIds)
                .subscribe(
                        success -> logger.info("Course access granted for purchase: {}", purchase.getId()),
                        error -> logger.error("Failed to grant course access for purchase: {}", purchase.getId(),
                                error));

        logger.info("Purchase completed successfully: {}", purchase.getId());
        return true;
    }

    @Transactional
    public boolean cancelPurchase(UUID purchaseId) {
        Optional<Purchase> purchaseOpt = purchaseRepository.findById(purchaseId);
        if (purchaseOpt.isEmpty()) {
            return false;
        }

        Purchase purchase = purchaseOpt.get();
        if (purchase.getStatus() != Purchase.PurchaseStatus.PENDING) {
            logger.warn("Cannot cancel purchase with status: {}", purchase.getStatus());
            return false;
        }

        purchase.setStatus(Purchase.PurchaseStatus.CANCELLED);
        purchaseRepository.save(purchase);

        // Cancel Stripe PaymentIntent if exists
        if (purchase.getStripePaymentIntentId() != null) {
            try {
                stripeService.cancelPaymentIntent(purchase.getStripePaymentIntentId());
            } catch (StripeException e) {
                logger.error("Failed to cancel Stripe PaymentIntent: {}", purchase.getStripePaymentIntentId(), e);
            }
        }

        logger.info("Purchase cancelled: {}", purchaseId);
        return true;
    }

    private PurchaseResponse convertToPurchaseResponse(Purchase purchase, String clientSecret) {
        PurchaseResponse response = new PurchaseResponse();
        response.setId(purchase.getId());
        response.setUserId(purchase.getUserId());
        response.setTotalAmount(purchase.getTotalAmount());
        response.setDiscountAmount(purchase.getDiscountAmount());
        response.setFinalAmount(purchase.getFinalAmount());
        response.setCurrency(purchase.getCurrency());
        response.setStatus(purchase.getStatus().name());
        response.setStripePaymentIntentId(purchase.getStripePaymentIntentId());
        response.setClientSecret(clientSecret);
        response.setCreatedAt(purchase.getCreatedAt());

        if (purchase.getItems() != null) {
            List<PurchaseResponse.PurchaseItemResponse> itemResponses = purchase.getItems().stream()
                    .map(this::convertToPurchaseItemResponse)
                    .collect(Collectors.toList());
            response.setItems(itemResponses);
        }

        return response;
    }

    private PurchaseResponse.PurchaseItemResponse convertToPurchaseItemResponse(PurchaseItem item) {
        PurchaseResponse.PurchaseItemResponse response = new PurchaseResponse.PurchaseItemResponse();
        response.setCourseId(item.getCourseId());
        response.setCourseTitle(item.getCourseTitle());
        response.setInstructorId(item.getInstructorId());
        response.setInstructorName(item.getInstructorName());
        response.setPrice(item.getPrice());
        response.setDiscountAmount(item.getDiscountAmount());
        response.setFinalPrice(item.getFinalPrice());
        return response;
    }
}
