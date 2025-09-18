package com.levelup.course_service.service;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CourseEnrollmentService {
    CourseEnrollmentResponseDTO enroll(CourseEnrollmentRequestDTO request);
    List<CourseEnrollmentResponseDTO> getEnrollmentsByUser(UUID userId);
    List<CourseEnrollmentResponseDTO> getEnrollmentsByCourse(UUID courseId);
    void updateProgress(UUID enrollmentId, List<String> completedLessons);
}