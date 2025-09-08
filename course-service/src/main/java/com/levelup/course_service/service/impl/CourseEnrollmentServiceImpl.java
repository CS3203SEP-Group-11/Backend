package com.levelup.course_service.service.impl;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;
import com.levelup.course_service.model.CourseEnrollment;
import com.levelup.course_service.model.Lesson;
import com.levelup.course_service.repository.CourseEnrollmentRepository;
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
    public List<CourseEnrollmentResponseDTO> getEnrollmentsByUser(String userId) {
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
}