package com.levelup.course_service.controller;

import com.levelup.course_service.dto.CertificateDTO;
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
            @RequestBody List<String> completedLessons) {
        enrollmentService.updateProgress(enrollmentId, completedLessons);
    }

    @GetMapping("/certificate/{enrollmentId}/request")
    public ResponseEntity<String> requestCertificate(@PathVariable UUID enrollmentId, @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(certificateService.requestCertificate(enrollmentId, currentUserId));
    }

    @GetMapping("/certificate/me")
    public List<CertificateDTO> getMyCertificates(@RequestHeader("X-User-ID") UUID currentUserId) {
        return certificateService.getCertificatesByUser(currentUserId);
    }

    @PostMapping("/{enrollmentId}/lesson/{lessonId}/complete")
    public ResponseEntity<String> completeLesson(@PathVariable UUID enrollmentId, @PathVariable UUID lessonId) {
        return ResponseEntity.ok(enrollmentService.completeLesson(enrollmentId, lessonId));
    }
}