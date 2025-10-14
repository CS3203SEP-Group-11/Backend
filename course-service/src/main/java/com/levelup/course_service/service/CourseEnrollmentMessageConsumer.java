package com.levelup.course_service.service;

import com.levelup.course_service.config.RabbitMQConfig;
import com.levelup.course_service.dto.CourseEnrollmentMessage;
import com.levelup.course_service.service.impl.CourseEnrollmentServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseEnrollmentMessageConsumer {

        private final CourseEnrollmentServiceImpl courseEnrollmentService;

        @RabbitListener(queues = RabbitMQConfig.COURSE_ENROLLMENT_QUEUE)
        @Transactional
        public void handleCourseEnrollmentMessage(CourseEnrollmentMessage message) {
                try {
                        log.info("Received course enrollment message for user: {} with {} courses",
                                        message.getUserId(), message.getCourseIds().size());
                        log.info("Message details - Status: {}, PurchaseTime: {}",
                                        message.getStatus(), message.getPurchaseTime());

                        // Create enrollment for each course using the service method
                        for (UUID courseId : message.getCourseIds()) {
                                // Use purchase time or current time as fallback
                                var enrollmentTime = message.getPurchaseTime() != null
                                                ? message.getPurchaseTime().toInstant(ZoneOffset.UTC)
                                                : java.time.Instant.now();

                                // Use the centralized enrollment method from service
                                courseEnrollmentService.createEnrollmentFromPayment(
                                                message.getUserId(),
                                                courseId,
                                                enrollmentTime);

                                log.info("Course enrollment processed for user: {} and course: {}",
                                                message.getUserId(), courseId);
                        }

                        log.info("Successfully processed course enrollment message for user: {}",
                                        message.getUserId());

                } catch (Exception e) {
                        log.error("Error processing course enrollment message: {}", e.getMessage(), e);
                        throw e; // Re-throw to trigger message requeue if needed
                }
        }
}