package com.levelup.course_service.dto;

import com.levelup.course_service.entity.Lesson;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonDTO {
    private UUID id;
    private UUID courseId;
    private String title;
    private String contentType;
    private String contentUrl;
    private String contentId; // Optional, if using cloud storage
    private String textContent;
    private UUID quizId;
    private int order;
    private Lesson.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}