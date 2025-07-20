package com.levelup.course_service.controller;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;
import com.levelup.course_service.service.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;

    @PostMapping
    public CourseEnrollmentResponseDTO enroll(@RequestBody CourseEnrollmentRequestDTO request) {
        return enrollmentService.enroll(request);
    }

    @GetMapping("/user/{userId}")
    public List<CourseEnrollmentResponseDTO> getByUser(@PathVariable String userId) {
        return enrollmentService.getEnrollmentsByUser(userId);
    }

    @GetMapping("/course/{courseId}")
    public List<CourseEnrollmentResponseDTO> getByCourse(@PathVariable String courseId) {
        return enrollmentService.getEnrollmentsByCourse(courseId);
    }

    @PutMapping("/{enrollmentId}/progress")
    public void updateProgress(
        @PathVariable String enrollmentId,
        @RequestBody List<String> completedLessons
    ) {
        enrollmentService.updateProgress(enrollmentId, completedLessons);
    }
}