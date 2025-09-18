package com.levelup.course_service.controller;

import com.levelup.course_service.dto.LessonDTO;
import com.levelup.course_service.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    public ResponseEntity<LessonDTO> createLesson(
            @RequestBody LessonDTO dto,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(lessonService.createLesson(dto, currentUserId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LessonDTO> updateLesson(
            @PathVariable UUID id,
            @RequestBody LessonDTO dto,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(lessonService.updateLesson(id, dto, currentUserId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LessonDTO> getLesson(@PathVariable UUID id) {
        return ResponseEntity.ok(lessonService.getLessonById(id));
    }

    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<LessonDTO>> getLessonsByCourse(@PathVariable UUID courseId) {
        return ResponseEntity.ok(lessonService.getLessonsByCourse(courseId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLesson(
            @PathVariable UUID id,
            @RequestHeader("X-User-ID") UUID currentUserId) {
        lessonService.deleteLesson(id, currentUserId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/state/{lessonId}")
    public ResponseEntity<String> changeLessonState(
            @PathVariable UUID lessonId,
            @RequestHeader("X-User-ID") UUID currentUserId,
            @RequestBody String status) {
        return ResponseEntity.ok(lessonService.changeLessonState(lessonId, currentUserId, status));
    }
}