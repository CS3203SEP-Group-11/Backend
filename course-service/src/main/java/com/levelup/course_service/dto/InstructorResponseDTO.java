package com.levelup.course_service.dto;

import lombok.Data;


@Data
public class InstructorResponseDTO {
    private String id;
    private boolean active;
    private boolean verified;
}