package com.levelup.notification_service.service;

import com.levelup.notification_service.client.UserServiceClient;
import com.levelup.notification_service.config.RabbitMQConfig;
import com.levelup.notification_service.dto.PaymentNotificationMessage;
import com.levelup.notification_service.entity.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationConsumer {

    private final NotificationService notificationService;
    private final EmailNotificationService emailNotificationService;
    private final UserServiceClient userServiceClient;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    @Transactional
    public void handlePaymentNotification(PaymentNotificationMessage message) {
        try {
            log.info("Received payment notification for user: {} with event: {}",
                    message.getUserId(), message.getEventType());

            // Create base notification
            Notification notification = notificationService.createNotification(
                    message.getUserId().toString(),
                    NotificationType.EMAIL,
                    buildNotificationContent(message));

            // Build email content
            String subject = buildEmailSubject(message.getEventType());
            String emailBody = buildEmailBody(message);

            // Get user email with error handling
            String userEmail = getUserEmailFromUserService(message.getUserId().toString());
            if (userEmail == null) {
                log.error("Cannot send email notification - user email not found for user: {}", message.getUserId());
                notificationService.updateNotificationStatus(notification.getId(), NotificationStatus.FAILED);
                return;
            }

            // Send email notification using existing email service
            boolean emailSent = emailNotificationService.sendEmail(userEmail, subject, emailBody);

            // Update notification status based on email send result
            if (emailSent) {
                notificationService.updateNotificationStatus(notification.getId(), NotificationStatus.SENT);

                // Create email notification record
                notificationService.createEmailNotification(
                        notification,
                        subject,
                        emailBody,
                        userEmail);

                log.info("Payment notification email sent successfully to user: {}", message.getUserId());
            } else {
                notificationService.updateNotificationStatus(notification.getId(), NotificationStatus.FAILED);
                log.error("Failed to send payment notification email to user: {}", message.getUserId());
            }

        } catch (Exception e) {
            log.error("Error processing payment notification: {}", e.getMessage(), e);
            // Don't re-throw to avoid infinite retry for non-recoverable errors
        }
    }

    private String buildNotificationContent(PaymentNotificationMessage message) {
        return switch (message.getEventType()) {
            case "PURCHASE_SUCCESS" -> String.format("Course purchase successful for %s %s",
                    message.getAmount(), message.getCurrency());
            case "PURCHASE_FAILED" -> String.format("Course purchase failed for %s %s",
                    message.getAmount(), message.getCurrency());
            case "SUBSCRIPTION_SUCCESS" -> String.format("Subscription payment successful for %s %s",
                    message.getAmount(), message.getCurrency());
            case "SUBSCRIPTION_FAILED" -> String.format("Subscription payment failed for %s %s",
                    message.getAmount(), message.getCurrency());
            default -> String.format("Payment notification for %s %s",
                    message.getAmount(), message.getCurrency());
        };
    }

    private String buildEmailSubject(String eventType) {
        return switch (eventType) {
            case "PURCHASE_SUCCESS" -> "Payment Successful - Course Purchase Confirmed";
            case "PURCHASE_FAILED" -> "Payment Failed - Course Purchase";
            case "SUBSCRIPTION_SUCCESS" -> "Subscription Active - Payment Successful";
            case "SUBSCRIPTION_FAILED" -> "Subscription Payment Failed";
            default -> "Payment Notification";
        };
    }

    private String buildEmailBody(PaymentNotificationMessage message) {
        return switch (message.getEventType()) {
            case "PURCHASE_SUCCESS" -> String.format("""
                    Dear Student,

                    Your payment of %s %s has been processed successfully!

                    Courses purchased: %s

                    You can now access your courses in your dashboard.

                    Thank you for choosing LevelUp!

                    Best regards,
                    LevelUp Team
                    """,
                    message.getAmount(),
                    message.getCurrency().toUpperCase(),
                    message.getCourseNames() != null ? String.join(", ", message.getCourseNames()) : "N/A");

            case "PURCHASE_FAILED" -> String.format("""
                    Dear Student,

                    We were unable to process your payment of %s %s for your course purchase.

                    Please check your payment method and try again, or contact our support team if the issue persists.

                    Best regards,
                    LevelUp Support Team
                    """,
                    message.getAmount(),
                    message.getCurrency().toUpperCase());

            case "SUBSCRIPTION_SUCCESS" -> String.format("""
                    Dear Student,

                    Congratulations! Your subscription payment of %s %s has been processed successfully.

                    Subscription: %s

                    Your subscription is now active and you have access to all premium courses and features.
                    %s

                    Thank you for choosing LevelUp Premium!

                    Best regards,
                    LevelUp Team
                    """,
                    message.getAmount(),
                    message.getCurrency().toUpperCase(),
                    message.getSubscriptionName() != null ? message.getSubscriptionName() : "Premium Subscription",
                    message.getInvoicePdfUrl() != null
                            ? "You can download your invoice from: " + message.getInvoicePdfUrl()
                            : "");

            case "SUBSCRIPTION_FAILED" -> String.format("""
                    Dear Student,

                    We were unable to process your subscription payment of %s %s.

                    Subscription: %s

                    Please check your payment method and try again, or contact our support team if the issue persists.

                    Your subscription will remain inactive until payment is successful.

                    Best regards,
                    LevelUp Support Team
                    """,
                    message.getAmount(),
                    message.getCurrency().toUpperCase(),
                    message.getSubscriptionName() != null ? message.getSubscriptionName() : "Premium Subscription");

            default -> String.format("""
                    Dear Student,

                    You have a payment notification regarding an amount of %s %s.

                    Please check your account for more details.

                    Best regards,
                    LevelUp Team
                    """,
                    message.getAmount(),
                    message.getCurrency().toUpperCase());
        };
    }

    private String getUserEmailFromUserService(String userId) {
        try {
            Map<String, Object> userResponse = userServiceClient.getUserById(userId);

            if (userResponse == null || userResponse.get("email") == null) {
                log.warn("User not found or email not available for user ID: {}", userId);
                return null;
            }

            String email = (String) userResponse.get("email");
            if (email == null || email.trim().isEmpty()) {
                log.warn("Email is empty for user ID: {}", userId);
                return null;
            }

            return email;
        } catch (feign.FeignException.NotFound e) {
            log.warn("User not found in user service for ID: {}", userId);
            return null;
        } catch (Exception e) {
            log.error("Failed to fetch user email for ID: {} - Error: {}", userId, e.getMessage());
            return null;
        }
    }
}