package com.levelup.user_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;
    private String email;
    private String firstName;
    private String lastName;
    private String profilePictureUrl;
}
