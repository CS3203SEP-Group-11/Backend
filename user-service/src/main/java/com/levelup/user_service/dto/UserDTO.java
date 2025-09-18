package com.levelup.user_service.dto;

import com.levelup.user_service.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserDTO {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private Role role;
    private LocalDate dateOfBirth;
    private String languagePreference;
}