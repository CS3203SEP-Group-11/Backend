package com.levelup.course_service.dto;


import lombok.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentResponseDTO {
    private UUID userId;
    private UUID courseId;
    private Instant enrollmentDate;
    private List<String> completedLessons;
    private double progressPercentage;
    private String status;
}