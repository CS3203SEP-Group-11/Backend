package com.levelup.course_service.controller;

import com.levelup.course_service.dto.CourseEnrollmentRequestDTO;
import com.levelup.course_service.dto.CourseEnrollmentResponseDTO;
import com.levelup.course_service.service.CertificateService;
import com.levelup.course_service.service.CourseEnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class CourseEnrollmentController {

    private final CourseEnrollmentService enrollmentService;
    private final CertificateService certificateService;

    @PostMapping
    public CourseEnrollmentResponseDTO enroll(@RequestBody CourseEnrollmentRequestDTO request) {
        return enrollmentService.enroll(request);
    }

    @GetMapping("/user/{userId}")
    public List<CourseEnrollmentResponseDTO> getByUser(@PathVariable UUID userId) {
        return enrollmentService.getEnrollmentsByUser(userId);
    }

    @GetMapping("/course/{courseId}")
    public List<CourseEnrollmentResponseDTO> getByCourse(@PathVariable UUID courseId) {
        return enrollmentService.getEnrollmentsByCourse(courseId);
    }

    @PutMapping("/{enrollmentId}/progress")
    public void updateProgress(
        @PathVariable UUID enrollmentId,
        @RequestBody List<String> completedLessons
    ) {
        enrollmentService.updateProgress(enrollmentId, completedLessons);
    }

    @GetMapping("/certificate/{enrollmentId}")
    public ResponseEntity<String> requestCertificate(@PathVariable UUID enrollmentId, @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(certificateService.requestCertificate(enrollmentId, currentUserId));
    }
}