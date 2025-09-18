package com.levelup.user_service.dto;

import com.levelup.user_service.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {
    private UUID userId;
    private String token;
    private String tokenType;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String profileImageUrl;
}