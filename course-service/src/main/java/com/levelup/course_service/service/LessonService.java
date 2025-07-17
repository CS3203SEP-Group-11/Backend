package com.levelup.course_service.service;

import java.util.List;

import com.levelup.course_service.dto.LessonDTO;

public interface LessonService {
    LessonDTO createLesson(LessonDTO dto);
    LessonDTO updateLesson(String id, LessonDTO dto);
    void deleteLesson(String id);
    LessonDTO getLessonById(String id);
    List<LessonDTO> getLessonsByCourse(String courseId);
}