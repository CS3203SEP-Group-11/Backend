package com.levelup.course_service.dto;


import lombok.*;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentResponseDTO {
    private String id;
    private String userId;
    private String courseId;
    private Instant enrollmentDate;
    private List<String> completedLessons;
    private int totalLessons;
    private double progressPercentage;
    private String status;
}