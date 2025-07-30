package com.levelup.assessment_service.dto;

import com.levelup.assessment_service.entity.QuizAttempt;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttemptDto {
    private UUID id;
    private UUID userId;
    private UUID quizId;
    private Integer attemptNumber;
    private BigDecimal score;
    private QuizAttempt.AttemptStatus status;
    private Boolean passed;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeTaken;
    private LocalDateTime createdAt;
    
    public static QuizAttemptDto fromEntity(QuizAttempt attempt) {
        QuizAttemptDto dto = new QuizAttemptDto();
        dto.setId(attempt.getId());
        dto.setUserId(attempt.getUserId());
        dto.setQuizId(attempt.getQuiz().getId());
        dto.setAttemptNumber(attempt.getAttemptNumber());
        dto.setScore(attempt.getScore());
        dto.setStatus(attempt.getStatus());
        dto.setPassed(attempt.getPassed());
        dto.setStartedAt(attempt.getStartedAt());
        dto.setCompletedAt(attempt.getCompletedAt());
        dto.setTimeTaken(attempt.getTimeTaken());
        dto.setCreatedAt(attempt.getCreatedAt());
        return dto;
    }
} 