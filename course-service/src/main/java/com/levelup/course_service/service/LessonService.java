package com.levelup.course_service.service;

import java.util.List;

import com.levelup.course_service.dto.LessonDTO;

public interface LessonService {
    LessonDTO createLesson(LessonDTO dto, String currentUserId);
    LessonDTO updateLesson(String id, LessonDTO dto, String currentUserId);
    void deleteLesson(String id, String currentUserId);
    LessonDTO getLessonById(String id);
    List<LessonDTO> getLessonsByCourse(String courseId);
}