package com.levelup.user_service.dto;

import com.levelup.user_service.entity.Role;
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
}