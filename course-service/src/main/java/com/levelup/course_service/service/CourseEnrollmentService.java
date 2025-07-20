package com.levelup.course_service.service;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;

import java.util.List;

public interface CourseEnrollmentService {
    CourseEnrollmentResponseDTO enroll(CourseEnrollmentRequestDTO request);
    List<CourseEnrollmentResponseDTO> getEnrollmentsByUser(String userId);
    List<CourseEnrollmentResponseDTO> getEnrollmentsByCourse(String courseId);
    void updateProgress(String enrollmentId, List<String> completedLessons);
}