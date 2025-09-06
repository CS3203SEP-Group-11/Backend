package com.levelup.user_service.dto;

import com.levelup.user_service.model.Instructor;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
@Builder
public class InstructorDTO {
    private String profileImageUrl;
    private String bio;
    private List<String> expertise;
    private ContactDetails contactDetails;
    private String instructorName;
    private Instant createdAt;

    @Data
    @Builder
    public static class ContactDetails {
        private String email;
        private String linkedin;
        private String website;
    }

}
