package com.levelup.course_service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private Role role;
    private LocalDate dateOfBirth;
    private String languagePreference;
    private String stripeCustomerId;
    private Boolean isSubscribed;

    public enum Role {
        STUDENT,
        INSTRUCTOR,
        ADMIN
    }
}