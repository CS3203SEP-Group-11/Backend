package com.levelup.user_service.dto;

import com.levelup.user_service.model.Role;
import lombok.Data;

import java.util.Date;

@Data
public class UserDTO {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private Boolean emailVerified;
    private String profileImageUrl;
    private Role role;
    private Date dateOfBirth;
    private String languagePreference;
}
