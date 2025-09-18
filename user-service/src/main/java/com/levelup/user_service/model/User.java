package com.levelup.user_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.Id;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uc_users_email", columnNames = "email")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @NotBlank
    @Email
    @Column(name = "email", length = 320, nullable = false)
    private String email;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @JsonIgnore
    @Column(name = "password_hash")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", length = 32)
    private AuthProvider authProvider;

    @Column(name = "google_id")
    private String googleId;

    @NotBlank
    @Column(name = "first_name", length = 100, nullable = false)
    private String firstName;

    @NotBlank
    @Column(name = "last_name", length = 100, nullable = false)
    private String lastName;

    @Column(name = "profile_image_url", length = 2048)
    private String profileImageUrl;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 32, nullable = false)
    private Role role;

    @Past
    @Column(name = "date_of_birth", columnDefinition = "date")
    private LocalDate dateOfBirth;

    @Column(name = "language_preference", length = 16)
    private String languagePreference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone")
    private Instant updatedAt;
}