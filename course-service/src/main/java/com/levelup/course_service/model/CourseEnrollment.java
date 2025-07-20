package com.levelup.course_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Document(collection = "course_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {
    @Id
    private String id;
    private String userId;
    private String courseId;
    private Instant enrollmentDate;
    private Progress progress;
    private Status status;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        COMPLETED, IN_PROGRESS
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Progress {
        private List<String> completedLessons;
        private int totalLessons;
        private double progressPercentage;
    }
}