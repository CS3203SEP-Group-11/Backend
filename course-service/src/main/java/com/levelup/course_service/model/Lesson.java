package com.levelup.course_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "lessons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 255)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type", nullable = false, length = 20)
    private ContentType contentType;

    @Column(name = "content_url")
    private String contentUrl;

    @Column(name = "content_id")
    private String contentId; // Optional, for cloud storage reference

    @Column(name = "text_content", columnDefinition = "TEXT")
    private String textContent;

    @Column(name = "quiz_id")
    private UUID quizId;

    @Column(name = "lesson_order")
    private int order;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Status status;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum ContentType {
        TEXT, VIDEO, QUIZ, PDF
    }

    public enum Status {
        DRAFT, PUBLISHED
    }
}
