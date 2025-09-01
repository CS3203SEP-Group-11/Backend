package com.levelup.user_service.dto;

import com.levelup.user_service.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String userId;
    private String token;
    private String tokenType;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String profilePictureUrl;
}
