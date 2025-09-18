package com.levelup.user_service.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InstructorDTO {
    private String bio;
    private String profileImageUrl;
    private List<String> expertise;
    private ContactDetails contactDetails;
    private String instructorName;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    public static class ContactDetails {
        private String email;
        private String linkedin;
        private String website;
    }
}