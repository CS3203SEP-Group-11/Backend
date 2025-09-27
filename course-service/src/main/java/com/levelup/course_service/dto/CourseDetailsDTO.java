package com.levelup.course_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDetailsDTO {
    private UUID id;
    private String title;
    private BigDecimal price;
    private UUID instructorId;
}