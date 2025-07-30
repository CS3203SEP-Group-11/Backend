package com.levelup.assessment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswerDto {
    private UUID id;
    private UUID questionId;
    private UUID selectedOptionId;
    private Boolean isCorrect;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 