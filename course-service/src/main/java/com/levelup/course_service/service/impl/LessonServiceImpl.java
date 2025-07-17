package com.levelup.course_service.service.impl;
import com.levelup.course_service.dto.LessonDTO;
import com.levelup.course_service.model.Lesson;
import com.levelup.course_service.repository.LessonRepository;
import com.levelup.course_service.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;

    @Override
    public LessonDTO createLesson(LessonDTO dto) {
        Lesson lesson = mapToEntity(dto);
        lesson.setCreatedAt(Instant.now());
        lesson.setUpdatedAt(Instant.now());
        return mapToDto(lessonRepository.save(lesson));
    }

    @Override
    public LessonDTO updateLesson(String id, LessonDTO dto) {
        Lesson existing = lessonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
        Lesson updated = mapToEntity(dto);
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        return mapToDto(lessonRepository.save(updated));
    }

    @Override
    public void deleteLesson(String id) {
        lessonRepository.deleteById(id);
    }

    @Override
    public LessonDTO getLessonById(String id) {
        return lessonRepository.findById(id).map(this::mapToDto)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    @Override
    public List<LessonDTO> getLessonsByCourse(String courseId) {
        return lessonRepository.findByCourseId(courseId)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    // === Mapping methods ===
    private Lesson mapToEntity(LessonDTO dto) {
        return Lesson.builder()
            .id(dto.getId())
            .courseId(dto.getCourseId())
            .title(dto.getTitle())
            .contentType(Lesson.ContentType.valueOf(dto.getContentType().toUpperCase()))
            .contentUrl(dto.getContentUrl().stream()
                .map(url -> new Lesson.ContentUrl(url.getType(), url.getUrl()))
                .collect(Collectors.toList()))
            .textContent(dto.getTextContent())
            .quizId(dto.getQuizId())
            .order(dto.getOrder())
            .status(Lesson.Status.valueOf(dto.getStatus().toUpperCase()))
            .build();
    }

    private LessonDTO mapToDto(Lesson entity) {
        return LessonDTO.builder()
            .id(entity.getId())
            .courseId(entity.getCourseId())
            .title(entity.getTitle())
            .contentType(entity.getContentType().name())
            .contentUrl(entity.getContentUrl().stream()
                .map(url -> new LessonDTO.ContentUrlDto(url.getType(), url.getUrl()))
                .collect(Collectors.toList()))
            .textContent(entity.getTextContent())
            .quizId(entity.getQuizId())
            .order(entity.getOrder())
            .status(entity.getStatus().name())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}