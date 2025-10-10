package com.levelup.user_service.dto;

import com.levelup.user_service.entity.ApplicationStatus;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstructorApplicationDTO {
    private UUID id;
    private UUID userId;
    private String applicantName;
    private Integer experienceYears;
    private String bio;
    private String expertise; // comma separated or free text
    private ApplicationStatus status;
    private Instant createdAt;
}
