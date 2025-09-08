package com.levelup.course_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "instructor_id")
    private String instructorId;

    @Column(length = 100)
    private String category;

    @ElementCollection
    @CollectionTable(name = "course_tags", joinColumns = @JoinColumn(name = "course_id"))
    @Column(name = "tag")
    private List<String> tags;

    @Column(length = 50)
    private String language;

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "thumbnail_id")
    private String thumbnailId;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "enrollment_count")
    private int enrollmentCount;

    @Column(name = "price_amount", precision = 12, scale = 2)
    private BigDecimal priceAmount;

    @Column(name = "price_currency", length = 10)
    private String priceCurrency;

    @Column(name = "rating_average", precision = 3, scale = 2)
    private BigDecimal ratingAverage;

    @Column(name = "rating_count")
    private int ratingCount;

    @Column(name = "duration")
    private Integer duration; // in hours

    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    public enum Status {
        DRAFT, PUBLISHED, ARCHIVED
    }

    public enum CourseLevel {
        BEGINNER, INTERMEDIATE, ADVANCED
    }
}
