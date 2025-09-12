package com.levelup.paymentservice.service;

import com.levelup.paymentservice.config.RabbitMQConfig;
import com.levelup.paymentservice.event.PaymentCompletedEvent;
import com.levelup.paymentservice.event.RefundProcessedEvent;
import com.levelup.paymentservice.event.SubscriptionCreatedEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EventPublishingService {

    private static final Logger logger = LoggerFactory.getLogger(EventPublishingService.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishPaymentCompletedEvent(PaymentCompletedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.PAYMENT_COMPLETED_KEY,
                    event);

            logger.info("Published PaymentCompletedEvent for purchase: {}", event.getPurchaseId());

            // Also publish notification event
            publishPaymentNotification(event);

        } catch (Exception e) {
            logger.error("Failed to publish PaymentCompletedEvent for purchase: {}", event.getPurchaseId(), e);
        }
    }

    public void publishSubscriptionCreatedEvent(SubscriptionCreatedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.SUBSCRIPTION_CREATED_KEY,
                    event);

            logger.info("Published SubscriptionCreatedEvent for subscription: {}", event.getSubscriptionId());

            // Also publish notification event
            publishSubscriptionNotification(event);

        } catch (Exception e) {
            logger.error("Failed to publish SubscriptionCreatedEvent for subscription: {}", event.getSubscriptionId(),
                    e);
        }
    }

    public void publishRefundProcessedEvent(RefundProcessedEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.PAYMENT_EXCHANGE,
                    RabbitMQConfig.REFUND_PROCESSED_KEY,
                    event);

            logger.info("Published RefundProcessedEvent for refund: {}", event.getRefundId());

            // Also publish notification event
            publishRefundNotification(event);

        } catch (Exception e) {
            logger.error("Failed to publish RefundProcessedEvent for refund: {}", event.getRefundId(), e);
        }
    }

    public void publishCourseAccessGrantedEvent(PaymentCompletedEvent paymentEvent) {
        try {
            CourseAccessEvent accessEvent = new CourseAccessEvent(
                    paymentEvent.getUserId(),
                    paymentEvent.getCourseIds(),
                    "GRANTED",
                    "PURCHASE_COMPLETED",
                    paymentEvent.getPurchaseId());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COURSE_EXCHANGE,
                    RabbitMQConfig.COURSE_ACCESS_GRANTED_KEY,
                    accessEvent);

            logger.info("Published CourseAccessGrantedEvent for user: {} and courses: {}",
                    paymentEvent.getUserId(), paymentEvent.getCourseIds());

        } catch (Exception e) {
            logger.error("Failed to publish CourseAccessGrantedEvent for user: {}", paymentEvent.getUserId(), e);
        }
    }

    public void publishCourseAccessRevokedEvent(RefundProcessedEvent refundEvent) {
        try {
            if (refundEvent.getSubscriptionId() != null) {
                // For subscription refunds, revoke access
                CourseAccessEvent accessEvent = new CourseAccessEvent(
                        refundEvent.getUserId(),
                        java.util.List.of(), // Course ID would need to be retrieved from subscription
                        "REVOKED",
                        "SUBSCRIPTION_REFUNDED",
                        refundEvent.getSubscriptionId());

                rabbitTemplate.convertAndSend(
                        RabbitMQConfig.COURSE_EXCHANGE,
                        RabbitMQConfig.COURSE_ACCESS_REVOKED_KEY,
                        accessEvent);

                logger.info("Published CourseAccessRevokedEvent for user: {} due to refund", refundEvent.getUserId());
            }
        } catch (Exception e) {
            logger.error("Failed to publish CourseAccessRevokedEvent for user: {}", refundEvent.getUserId(), e);
        }
    }

    private void publishPaymentNotification(PaymentCompletedEvent event) {
        try {
            PaymentNotificationEvent notification = new PaymentNotificationEvent(
                    event.getUserId(),
                    "PAYMENT_COMPLETED",
                    "Payment Successful",
                    String.format("Your payment of %s %s has been processed successfully.",
                            event.getCurrency(), event.getFinalAmount()),
                    "EMAIL,PUSH");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.PAYMENT_NOTIFICATION_KEY,
                    notification);

        } catch (Exception e) {
            logger.error("Failed to publish payment notification", e);
        }
    }

    private void publishSubscriptionNotification(SubscriptionCreatedEvent event) {
        try {
            PaymentNotificationEvent notification = new PaymentNotificationEvent(
                    event.getUserId(),
                    "SUBSCRIPTION_CREATED",
                    "Subscription Activated",
                    String.format("Your subscription to '%s' has been activated successfully.",
                            event.getCourseTitle()),
                    "EMAIL,PUSH");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.PAYMENT_NOTIFICATION_KEY,
                    notification);

        } catch (Exception e) {
            logger.error("Failed to publish subscription notification", e);
        }
    }

    private void publishRefundNotification(RefundProcessedEvent event) {
        try {
            PaymentNotificationEvent notification = new PaymentNotificationEvent(
                    event.getUserId(),
                    "REFUND_PROCESSED",
                    "Refund Processed",
                    String.format(
                            "Your refund of %s %s has been processed and will appear in your account within 5-10 business days.",
                            event.getCurrency(), event.getRefundAmount()),
                    "EMAIL,PUSH");

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.PAYMENT_NOTIFICATION_KEY,
                    notification);

        } catch (Exception e) {
            logger.error("Failed to publish refund notification", e);
        }
    }

    // Inner classes for other event types
    public static class CourseAccessEvent {
        private java.util.UUID userId;
        private java.util.List<java.util.UUID> courseIds;
        private String accessType;
        private String reason;
        private java.util.UUID referenceId;
        private java.time.LocalDateTime timestamp;

        public CourseAccessEvent(java.util.UUID userId, java.util.List<java.util.UUID> courseIds,
                String accessType, String reason, java.util.UUID referenceId) {
            this.userId = userId;
            this.courseIds = courseIds;
            this.accessType = accessType;
            this.reason = reason;
            this.referenceId = referenceId;
            this.timestamp = java.time.LocalDateTime.now();
        }

        // Getters and setters
        public java.util.UUID getUserId() {
            return userId;
        }

        public void setUserId(java.util.UUID userId) {
            this.userId = userId;
        }

        public java.util.List<java.util.UUID> getCourseIds() {
            return courseIds;
        }

        public void setCourseIds(java.util.List<java.util.UUID> courseIds) {
            this.courseIds = courseIds;
        }

        public String getAccessType() {
            return accessType;
        }

        public void setAccessType(String accessType) {
            this.accessType = accessType;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }

        public java.util.UUID getReferenceId() {
            return referenceId;
        }

        public void setReferenceId(java.util.UUID referenceId) {
            this.referenceId = referenceId;
        }

        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(java.time.LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class PaymentNotificationEvent {
        private java.util.UUID userId;
        private String eventType;
        private String title;
        private String message;
        private String channels;
        private java.time.LocalDateTime timestamp;

        public PaymentNotificationEvent(java.util.UUID userId, String eventType,
                String title, String message, String channels) {
            this.userId = userId;
            this.eventType = eventType;
            this.title = title;
            this.message = message;
            this.channels = channels;
            this.timestamp = java.time.LocalDateTime.now();
        }

        // Getters and setters
        public java.util.UUID getUserId() {
            return userId;
        }

        public void setUserId(java.util.UUID userId) {
            this.userId = userId;
        }

        public String getEventType() {
            return eventType;
        }

        public void setEventType(String eventType) {
            this.eventType = eventType;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getChannels() {
            return channels;
        }

        public void setChannels(String channels) {
            this.channels = channels;
        }

        public java.time.LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(java.time.LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
