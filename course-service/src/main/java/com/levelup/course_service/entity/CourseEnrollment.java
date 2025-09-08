package com.levelup.course_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "course_enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Column(name = "enrollment_date", nullable = false)
    private Instant enrollmentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ElementCollection
    @CollectionTable(
            name = "enrollment_completed_lessons",
            joinColumns = @JoinColumn(name = "enrollment_id")
    )
    @Column(name = "lesson_id")
    private List<UUID> completedLessons;

    @Column(name = "progress_percentage", nullable = false)
    private double progressPercentage;

    public enum Status {
        COMPLETED, IN_PROGRESS
    }
}