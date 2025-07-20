package com.levelup.course_service.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseEnrollmentRequestDTO {
    private String userId;
    private String courseId;
}