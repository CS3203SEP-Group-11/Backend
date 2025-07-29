package com.levelup.course_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.service.CourseService;
import com.levelup.course_service.dto.CourseDTO;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    public ResponseEntity<Course> createCourse(
            @RequestBody CourseDTO dto,
            @RequestHeader("X-User-ID") String instructorId) {
        return ResponseEntity.ok(courseService.createCourse(dto, instructorId));
    }

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable String id) {
        return courseService.getCourseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable String id, 
            @RequestBody CourseDTO dto,
            @RequestHeader("X-User-ID") String instructorId) {
        return ResponseEntity.ok(courseService.updateCourse(id, dto, instructorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String instructorId) {
        courseService.deleteCourse(id, instructorId);
        return ResponseEntity.noContent().build();
    }
}