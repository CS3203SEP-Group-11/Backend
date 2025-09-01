package com.levelup.user_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    String id = UUID.randomUUID().toString();

    @NotBlank
    @Email
    private String email;

    private Boolean emailVerified;

    @JsonIgnore
    private String password;

    private AuthProvider authProvider;

    private String googleId;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String profileImageUrl;

    @NotBlank
    private Role role;

    @Past
    private Date dateOfBirth;

    private String languagePreference;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
