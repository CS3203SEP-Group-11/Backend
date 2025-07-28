package com.levelup.assessment_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmitAnswersRequest {
    
    @NotEmpty(message = "Answers list cannot be empty")
    @Valid
    private List<UserAnswerSubmission> answers;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserAnswerSubmission {
        private UUID questionId;
        private UUID selectedOptionId; // null for non-multiple choice questions
    }
} 