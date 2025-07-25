package com.levelup.user_service.dto;

import com.levelup.user_service.model.Role;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class UserDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String profileImageUrl;
    private Role role;
    private Date dateOfBirth;
    private String languagePreference;
}
