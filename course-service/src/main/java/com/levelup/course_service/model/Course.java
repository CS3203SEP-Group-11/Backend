package com.levelup.course_service.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Document(collection = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private String id;

    private String title;
    private String description;
    private String instructorId;
    private String category;
    private List<String> tags;
    private String language;
    private String thumbnailUrl;
    private Status status;
    private Instant publishedAt;
    private int enrollmentCount;

    private Price price;
    private Rating rating;

    private Instant createdAt;
    private Instant updatedAt;

    public enum Status {
        DRAFT, PUBLISHED, ARCHIVED
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Price {
        private BigDecimal amount;
        private String currency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Rating {
        private BigDecimal average;
        private int count;
    }
}