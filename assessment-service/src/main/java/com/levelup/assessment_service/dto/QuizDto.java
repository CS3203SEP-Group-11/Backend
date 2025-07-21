package com.levelup.assessment_service.dto;

import com.levelup.assessment_service.entity.Quiz;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizDto {
    private UUID id;
    private UUID lessonId;
    private String title;
    private String description;
    private BigDecimal passingScore;
    private Integer timeLimit;
    private Integer attemptLimit;
    private List<QuestionDto> questions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static QuizDto fromEntity(Quiz quiz) {
        QuizDto dto = new QuizDto();
        dto.setId(quiz.getId());
        dto.setLessonId(quiz.getLessonId());
        dto.setTitle(quiz.getTitle());
        dto.setDescription(quiz.getDescription());
        dto.setPassingScore(quiz.getPassingScore());
        dto.setTimeLimit(quiz.getTimeLimit());
        dto.setAttemptLimit(quiz.getAttemptLimit());
        dto.setCreatedAt(quiz.getCreatedAt());
        dto.setUpdatedAt(quiz.getUpdatedAt());
        return dto;
    }
} 