package com.levelup.course_service.service.impl;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;
import com.levelup.course_service.model.CourseEnrollment;
import com.levelup.course_service.repository.CourseEnrollmentRepository;
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

    private final CourseEnrollmentRepository repository;

    @Override
    public CourseEnrollmentResponseDTO enroll(CourseEnrollmentRequestDTO request) {
        if (repository.existsByUserIdAndCourseId(request.getUserId(), request.getCourseId())) {
            throw new IllegalArgumentException("User already enrolled in this course.");
        }

        CourseEnrollment enrollment = CourseEnrollment.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .courseId(request.getCourseId())
                .enrollmentDate(Instant.now())
                .progress(new CourseEnrollment.Progress(List.of(), 0, 0.0))
                .status(CourseEnrollment.Status.IN_PROGRESS)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        repository.save(enrollment);
        return toDto(enrollment);
    }

    @Override
    public List<CourseEnrollmentResponseDTO> getEnrollmentsByUser(String userId) {
        return repository.findByUserId(userId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public List<CourseEnrollmentResponseDTO> getEnrollmentsByCourse(String courseId) {
        return repository.findByCourseId(courseId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public void updateProgress(String enrollmentId, List<String> completedLessons) {
        CourseEnrollment enrollment = repository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        int totalLessons = enrollment.getProgress().getTotalLessons();
        double progress = totalLessons == 0 ? 0.0 :
                ((double) completedLessons.size() / totalLessons) * 100;

        enrollment.getProgress().setCompletedLessons(completedLessons);
        enrollment.getProgress().setProgressPercentage(progress);
        enrollment.setUpdatedAt(Instant.now());

        if (progress == 100.0) {
            enrollment.setStatus(CourseEnrollment.Status.COMPLETED);
        }

        repository.save(enrollment);
    }

    private CourseEnrollmentResponseDTO toDto(CourseEnrollment enrollment) {
        return CourseEnrollmentResponseDTO.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUserId())
                .courseId(enrollment.getCourseId())
                .enrollmentDate(enrollment.getEnrollmentDate())
                .completedLessons(enrollment.getProgress().getCompletedLessons())
                .totalLessons(enrollment.getProgress().getTotalLessons())
                .progressPercentage(enrollment.getProgress().getProgressPercentage())
                .status(enrollment.getStatus().name())
                .build();
    }
}