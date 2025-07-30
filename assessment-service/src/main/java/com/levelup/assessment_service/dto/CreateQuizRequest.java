package com.levelup.assessment_service.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuizRequest {
    
    @NotBlank(message = "Quiz title is required")
    private String title;
    
    private String description;
    
    @NotNull(message = "Lesson ID is required")
    private UUID lessonId;
    
    @NotNull(message = "Time limit is required")
    @Min(value = 1, message = "Time limit must be at least 1 minute")
    private Integer timeLimit;
    
    @NotNull(message = "Attempt limit is required")
    @Min(value = 1, message = "Attempt limit must be at least 1")
    private Integer attemptLimit;
    
    @NotNull(message = "Passing score is required")
    @DecimalMin(value = "0.0", message = "Passing score must be non-negative")
    private BigDecimal passingScore;
} 