package com.levelup.course_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    private String id;

    private String courseId;
    private String title;
    private ContentType contentType;
    private List<ContentUrl> contentUrl;
    private String textContent;
    private String quizId;
    private int order;
    private Status status;

    private Instant createdAt;
    private Instant updatedAt;

    public enum ContentType {
        TEXT, VIDEO, QUIZ, PDF, DOCX
    }

    public enum Status {
        DRAFT, PUBLISHED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContentUrl {
        private String type; // e.g., 'video', 'pdf'
        private String url;
    }
}