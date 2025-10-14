package com.levelup.course_service.service.impl;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;
import com.levelup.course_service.entity.Course;
import com.levelup.course_service.entity.CourseEnrollment;
import com.levelup.course_service.entity.Lesson;
import com.levelup.course_service.repository.CourseEnrollmentRepository;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.repository.LessonRepository;
import com.levelup.course_service.service.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseEnrollmentServiceImpl implements CourseEnrollmentService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;

    @Override
    public CourseEnrollmentResponseDTO enroll(CourseEnrollmentRequestDTO request) {
        if (enrollmentRepository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new IllegalArgumentException("User already enrolled in this course.");
        }

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .enrollmentDate(Instant.now())
                .progressPercentage(0.0)
                .status(CourseEnrollment.Status.IN_PROGRESS)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        enrollmentRepository.save(enrollment);
        return toDto(enrollment);
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

        double progress = totalLessons == 0 ? 0.0 :
                ((double) completedLessons.size() / totalLessons) * 100;

        enrollment.setCompletedLessons(
                completedLessons.stream()
                        .map(UUID::fromString)
                        .collect(Collectors.toList())
        );

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