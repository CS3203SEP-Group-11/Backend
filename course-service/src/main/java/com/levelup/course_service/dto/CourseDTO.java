package com.levelup.course_service.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CourseDTO {
    private String title;
    private String description;
    private String instructorId;
    private String category;
    private List<String> tags;
    private String language;
    private String thumbnailUrl;
    private String status;
    private BigDecimal priceAmount;
    private String priceCurrency;
}