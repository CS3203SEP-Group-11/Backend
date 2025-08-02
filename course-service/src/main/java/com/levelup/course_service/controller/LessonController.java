package com.levelup.course_service.controller;

import com.levelup.course_service.dto.LessonDTO;
import com.levelup.course_service.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(
            @RequestBody LessonDTO dto,
            @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(dto, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable String id, 
            @RequestBody LessonDTO dto,
            @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(lessonService.updateLesson(id, dto, currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLesson(@PathVariable String id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourse(@PathVariable String courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable String id,
            @RequestHeader("X-User-ID") String currentUserId) {
        lessonService.deleteLesson(id, currentUserId);
        return ResponseEntity.noContent().build();
    }
}