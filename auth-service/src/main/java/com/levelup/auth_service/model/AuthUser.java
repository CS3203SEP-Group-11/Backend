package com.levelup.auth_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "auth_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {
    @Id
    String userId = UUID.randomUUID().toString();

    @NotBlank
    @Email
    private String email;

    private Boolean emailVerified;

    @JsonIgnore
    private String password;

    private AuthProvider authProvider;
    private String googleId;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}

