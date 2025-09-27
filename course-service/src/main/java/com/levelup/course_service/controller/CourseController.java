package com.levelup.course_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import com.levelup.course_service.entity.Course;
import com.levelup.course_service.service.CourseService;
import com.levelup.course_service.dto.CourseDTO;
import com.levelup.course_service.dto.CourseDetailsRequestDTO;
import com.levelup.course_service.dto.CourseDetailsResponseDTO;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Slf4j
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @RequestBody CourseDTO dto,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        log.info("Creating course for user: {}", currentUserId);
        return ResponseEntity.ok(courseService.createCourse(dto, currentUserId));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable UUID id) {
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable UUID id,
            @RequestBody CourseDTO dto,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto, currentUserId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        courseService.deleteCourse(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCoursesByCategory() {
        return ResponseEntity.ok(courseService.getAllCategory());
    }

    @PutMapping("/state/{status}/{courseId}")
    public ResponseEntity<String> changeCourseState(@PathVariable UUID courseId, @PathVariable String status,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(courseService.changeCourseState(courseId, currentUserId, status));
    }

    @GetMapping("/instructor/me")
    public ResponseEntity<List<Course>> getCoursesByInstructor(@RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(courseService.getMyCourses(currentUserId));
    }

    @PostMapping("/details")
    public ResponseEntity<CourseDetailsResponseDTO> getCourseDetails(@RequestBody CourseDetailsRequestDTO request) {
        log.info("Fetching course details for IDs: {}", request.getCourseIds());
        return ResponseEntity.ok(courseService.getCourseDetailsByIds(request.getCourseIds()));
    }
}