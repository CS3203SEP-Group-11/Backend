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

    @Override
    public String completeLesson(UUID enrollmentId, UUID lessonId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (enrollment.getCompletedLessons().contains(lessonId)) {
            return "Lesson already completed.";
        }

        List<Lesson> courseLessons = lessonRepository.findByCourseId(enrollment.getCourseId());
        int totalLessons = courseLessons.size();

        List<UUID> updatedCompletedLessons = new ArrayList<>(enrollment.getCompletedLessons());
        updatedCompletedLessons.add(lessonId);
        enrollment.setCompletedLessons(updatedCompletedLessons);

        double progress = totalLessons == 0 ? 0.0 : ((double) updatedCompletedLessons.size() / totalLessons) * 100;
        enrollment.setProgressPercentage(progress);
        enrollment.setUpdatedAt(Instant.now());

        if (progress == 100.0) {
            enrollment.setStatus(CourseEnrollment.Status.COMPLETED);
        }

        enrollmentRepository.save(enrollment);
        return "Lesson marked as completed.";
    }

    private CourseEnrollmentResponseDTO toDto(CourseEnrollment enrollment) {
        return CourseEnrollmentResponseDTO.builder()
                .id(enrollment.getId())
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

    @Override
    public java.util.Map<String, Object> getEnrollmentAnalytics() {
        java.util.Map<String, Object> analytics = new java.util.HashMap<>();
        
        List<CourseEnrollment> allEnrollments = enrollmentRepository.findAll();
        List<Course> allCourses = courseRepository.findAll();
        
        // Basic enrollment statistics
        int totalEnrollments = allEnrollments.size();
        long totalCompletions = allEnrollments.stream()
                .mapToLong(enrollment -> enrollment.getStatus() == CourseEnrollment.Status.COMPLETED ? 1 : 0)
                .sum();
        
        double completionRate = totalEnrollments > 0 ? (double) totalCompletions / totalEnrollments : 0.0;
        
        analytics.put("totalEnrollments", totalEnrollments);
        analytics.put("totalCompletions", totalCompletions);
        analytics.put("completionRate", Math.round(completionRate * 1000) / 1000.0);
        
        // Category-wise enrollment and completion statistics
        java.util.Map<String, Integer> categoryEnrollments = new java.util.HashMap<>();
        java.util.Map<String, Integer> categoryCompletions = new java.util.HashMap<>();
        
        // Get category mapping from courses
        java.util.Map<UUID, String> courseCategories = new java.util.HashMap<>();
        for (Course course : allCourses) {
            if (course.getId() != null && course.getCategory() != null) {
                courseCategories.put(course.getId(), course.getCategory());
            }
        }
        
        // Count enrollments and completions by category
        for (CourseEnrollment enrollment : allEnrollments) {
            String category = courseCategories.get(enrollment.getCourseId());
            if (category != null) {
                categoryEnrollments.put(category, categoryEnrollments.getOrDefault(category, 0) + 1);
                if (enrollment.getStatus() == CourseEnrollment.Status.COMPLETED) {
                    categoryCompletions.put(category, categoryCompletions.getOrDefault(category, 0) + 1);
                }
            }
        }
        
        // Create category enrollment list
        java.util.List<java.util.Map<String, Object>> categoryEnrollmentsList = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : categoryEnrollments.entrySet()) {
            java.util.Map<String, Object> categoryData = new java.util.HashMap<>();
            categoryData.put("category", entry.getKey());
            categoryData.put("enrollments", entry.getValue());
            categoryData.put("completions", categoryCompletions.getOrDefault(entry.getKey(), 0));
            categoryEnrollmentsList.add(categoryData);
        }
        
        // Sort by enrollments descending
        categoryEnrollmentsList.sort((a, b) -> Integer.compare((Integer)b.get("enrollments"), (Integer)a.get("enrollments")));
        
        analytics.put("categoryEnrollments", categoryEnrollmentsList);
        
        return analytics;
    }
}