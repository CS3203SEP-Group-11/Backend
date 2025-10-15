package com.levelup.course_service.service;

import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;

import java.util.List;
import java.util.UUID;

public interface CourseEnrollmentService {
    List<CourseEnrollmentResponseDTO> getEnrollmentsByUser(UUID userId);

    List<CourseEnrollmentResponseDTO> getEnrollmentsByCourse(UUID courseId);

    void updateProgress(UUID enrollmentId, List<String> completedLessons);
    
    java.util.Map<String, Object> getEnrollmentAnalytics();

    String completeLesson(UUID enrollmentId, UUID lessonId);
}