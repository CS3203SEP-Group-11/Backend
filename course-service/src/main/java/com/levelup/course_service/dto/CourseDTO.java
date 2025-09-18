package com.levelup.course_service.dto;

import com.levelup.course_service.entity.Course;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CourseDTO {
    private String title;
    private String description;
    private UUID instructorId;
    private String category;
    private List<String> tags;
    private String language;
    private String thumbnailUrl;
    private String thumbnailId; // Optional, if using cloud storage
    private String status;
    private Integer duration; // in hours
    private Course.CourseLevel level; // Beginner, Intermediate, Advanced
    private BigDecimal priceAmount;
    private String priceCurrency;
}