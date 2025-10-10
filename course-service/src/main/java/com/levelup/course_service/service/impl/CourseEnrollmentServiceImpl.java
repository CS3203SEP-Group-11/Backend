package com.levelup.course_service.service.impl;

import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;
import com.levelup.course_service.entity.Course;
import com.levelup.course_service.entity.CourseEnrollment;
import com.levelup.course_service.entity.Lesson;
import com.levelup.course_service.repository.CourseEnrollmentRepository;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.repository.LessonRepository;
import com.levelup.course_service.service.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final LessonRepository lessonRepository;

    /**
     * Create enrollment from RabbitMQ message (payment success)
     * This is the primary enrollment method used by the system
     */
    @Transactional
    public CourseEnrollment createEnrollmentFromPayment(UUID userId, UUID courseId, Instant enrollmentTime) {
        log.info("Creating enrollment for user: {} in course: {}", userId, courseId);

        // Check if enrollment already exists
        if (enrollmentRepository.existsByUserIdAndCourseId(userId, courseId)) {
            log.info("Enrollment already exists for user: {} and course: {}", userId, courseId);
            return enrollmentRepository.findByUserIdAndCourseId(userId, courseId).orElse(null);
        }

        // Create new enrollment
        CourseEnrollment enrollment = CourseEnrollment.builder()
                .userId(userId)
                .courseId(courseId)
                .enrollmentDate(enrollmentTime)
                .status(CourseEnrollment.Status.IN_PROGRESS)
                .createdAt(Instant.now())
                .completedLessons(new ArrayList<>())
                .progressPercentage(0.0)
                .build();

        enrollment = enrollmentRepository.save(enrollment);
        log.info("Enrollment created with ID: {}", enrollment.getId());

        // Update course enrollment count
        updateCourseEnrollmentCount(courseId);

        return enrollment;
    }

    /**
     * Update enrollment count in course table
     */
    @Transactional
    public void updateCourseEnrollmentCount(UUID courseId) {
        Course course = courseRepository.findById(courseId).orElse(null);
        if (course != null) {
            int enrollmentCount = enrollmentRepository.countByCourseId(courseId);
            course.setEnrollmentCount(enrollmentCount);
            course.setUpdatedAt(Instant.now());
            courseRepository.save(course);
            log.info("Updated enrollment count for course {}: {}", courseId, enrollmentCount);
        }
    }

    @Override
    public List<CourseEnrollmentResponseDTO> getEnrollmentsByUser(UUID userId) {
        return enrollmentRepository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CourseEnrollmentResponseDTO> getEnrollmentsByCourse(UUID courseId) {
        return enrollmentRepository.findByCourseId(courseId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void updateProgress(UUID enrollmentId, List<String> completedLessons) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        List<Lesson> courseLessons = lessonRepository.findByCourseId(enrollment.getCourseId());

        int totalLessons = courseLessons.size();

        double progress = totalLessons == 0 ? 0.0 : ((double) completedLessons.size() / totalLessons) * 100;

        enrollment.setCompletedLessons(
                completedLessons.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList()));

        enrollment.setProgressPercentage(progress);
        enrollment.setUpdatedAt(Instant.now());

        if (progress == 100.0) {
            enrollment.setStatus(CourseEnrollment.Status.COMPLETED);
        }

        enrollmentRepository.save(enrollment);
    }

    private CourseEnrollmentResponseDTO toDto(CourseEnrollment enrollment) {
        return CourseEnrollmentResponseDTO.builder()
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .completedLessons(enrollment.getCompletedLessons().stream()
                        .map(UUID::toString)
                        .collect(Collectors.toList()))
                .progressPercentage(enrollment.getProgressPercentage())
                .status(enrollment.getStatus().name())
                .build();
    }
}