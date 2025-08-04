package com.levelup.course_service.dto;

import com.levelup.course_service.model.Lesson;
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
    private Lesson.Status status;
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