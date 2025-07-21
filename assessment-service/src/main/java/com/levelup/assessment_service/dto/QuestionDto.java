package com.levelup.assessment_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDto {
    private UUID id;
    private String questionText;
    private Integer order;
    private List<QuestionOptionDto> options;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static QuestionDto fromEntity(com.levelup.assessment_service.entity.Question question) {
        QuestionDto dto = new QuestionDto();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setOrder(question.getOrder());
        dto.setCreatedAt(question.getCreatedAt());
        dto.setUpdatedAt(question.getUpdatedAt());
        return dto;
    }
} 