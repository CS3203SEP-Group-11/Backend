package com.levelup.payment_service.service;

import com.levelup.payment_service.config.RabbitMQConfig;
import com.levelup.payment_service.dto.message.CourseEnrollmentMessage;
import com.levelup.payment_service.dto.message.PaymentNotificationMessage;
import com.levelup.payment_service.dto.message.SubscriptionMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessagePublisherService {

    private final RabbitTemplate rabbitTemplate;

    public void sendCourseEnrollmentMessage(CourseEnrollmentMessage message) {
        try {
            log.info("Sending course enrollment message for user: {} and courses: {}",
                    message.getUserId(), message.getCourseIds());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COURSE_ENROLLMENT_EXCHANGE,
                    RabbitMQConfig.COURSE_ENROLLMENT_ROUTING_KEY,
                    message);

            log.info("Course enrollment message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send course enrollment message", e);
            throw new RuntimeException("Failed to send course enrollment message", e);
        }
    }

    public void sendSubscriptionMessage(SubscriptionMessage message) {
        try {
            log.info("Sending subscription message for user: {} with subscription: {} and status: {}",
                    message.getUserId(), message.getSubscriptionName(), message.getStatus());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.COURSE_ENROLLMENT_EXCHANGE,
                    RabbitMQConfig.COURSE_ENROLLMENT_ROUTING_KEY,
                    message);

            log.info("Subscription message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send subscription message", e);
            throw new RuntimeException("Failed to send subscription message", e);
        }
    }

    public void sendPaymentNotificationMessage(PaymentNotificationMessage message) {
        try {
            log.info("Sending payment notification message for user: {} with event: {}",
                    message.getUserId(), message.getEventType());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message);

            log.info("Payment notification message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send payment notification message", e);
            throw new RuntimeException("Failed to send payment notification message", e);
        }
    }

    public void sendSubscriptionNotificationMessage(SubscriptionMessage message) {
        try {
            log.info("Sending subscription notification message for user: {} with status: {}",
                    message.getUserId(), message.getStatus());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    message);

            log.info("Subscription notification message sent successfully");
        } catch (Exception e) {
            log.error("Failed to send subscription notification message", e);
            throw new RuntimeException("Failed to send subscription notification message", e);
        }
    }
}
