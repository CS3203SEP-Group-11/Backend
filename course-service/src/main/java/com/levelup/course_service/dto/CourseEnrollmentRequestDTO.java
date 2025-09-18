package com.levelup.course_service.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentRequestDTO {
    private UUID userId;
    private UUID courseId;
}