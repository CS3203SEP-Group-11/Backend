package com.levelup.course_service.service;

import com.levelup.course_service.config.RabbitMQConfig;
import com.levelup.course_service.dto.CourseEnrollmentMessage;
import com.levelup.course_service.entity.CourseEnrollment;
import com.levelup.course_service.repository.CourseEnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseEnrollmentMessageConsumer {

    private final CourseEnrollmentRepository courseEnrollmentRepository;

    @RabbitListener(queues = RabbitMQConfig.COURSE_ENROLLMENT_QUEUE)
    @Transactional
    public void handleCourseEnrollmentMessage(CourseEnrollmentMessage message) {
        try {
            log.info("Received course enrollment message for user: {} with {} courses",
                    message.getUserId(), message.getCourseIds().size());
            log.info("Message details - Status: {}, PurchaseTime: {}",
                    message.getStatus(), message.getPurchaseTime());

            // Create enrollment for each course
            for (UUID courseId : message.getCourseIds()) {
                // Check if enrollment already exists
                boolean exists = courseEnrollmentRepository
                        .existsByUserIdAndCourseId(message.getUserId(), courseId);

                if (!exists) {
                    // Use purchase time or current time as fallback
                    Instant enrollmentTime = message.getPurchaseTime() != null
                            ? message.getPurchaseTime().toInstant(ZoneOffset.UTC)
                            : Instant.now();

                    CourseEnrollment enrollment = CourseEnrollment.builder()
                            .userId(message.getUserId())
                            .courseId(courseId)
                            .enrollmentDate(enrollmentTime)
                            .status(CourseEnrollment.Status.IN_PROGRESS)
                            .createdAt(Instant.now())
                            .completedLessons(new ArrayList<>())
                            .progressPercentage(0.0)
                            .build();

                    courseEnrollmentRepository.save(enrollment);
                    log.info("Course enrollment created for user: {} and course: {}",
                            message.getUserId(), courseId);
                } else {
                    log.info("Course enrollment already exists for user: {} and course: {}",
                            message.getUserId(), courseId);
                }
            }

            log.info("Successfully processed course enrollment message for user: {}",
                    message.getUserId());

        } catch (Exception e) {
            log.error("Error processing course enrollment message: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger message requeue if needed
        }
    }
}