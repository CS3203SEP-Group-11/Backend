package com.levelup.course_service.dto;

import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDTO {
    private String id;
    private String courseId;
    private String title;
    private String contentType;
    private List<ContentUrlDto> contentUrl;
    private String textContent;
    private String quizId;
    private int order;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentUrlDto {
        private String type;
        private String url;
    }
}