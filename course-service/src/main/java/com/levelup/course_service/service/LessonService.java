package com.levelup.course_service.service;

import java.util.List;
import java.util.UUID;

import com.levelup.course_service.dto.LessonDTO;

public interface LessonService {
    LessonDTO createLesson(LessonDTO dto, UUID currentUserId);
    LessonDTO updateLesson(UUID id, LessonDTO dto, UUID currentUserId);
    void deleteLesson(UUID id, UUID currentUserId);
    LessonDTO getLessonById(UUID id);
    List<LessonDTO> getLessonsByCourse(UUID courseId);
    String changeLessonState(UUID lessonId, UUID currentUserId, String status);
}