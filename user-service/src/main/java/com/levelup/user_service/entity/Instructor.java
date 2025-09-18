package com.levelup.user_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "instructors",
        indexes = {
                @Index(name = "idx_instructors_user_id", columnList = "user_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Instructor {

    @Id
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            columnDefinition = "uuid",
            foreignKey = @ForeignKey(name = "fk_instructors_user")
    )
    @ToString.Exclude
    private User user;

    @Column(name = "bio", columnDefinition = "text")
    private String bio;

    @ElementCollection
    @CollectionTable(
            name = "instructor_expertise",
            joinColumns = @JoinColumn(name = "instructor_id")
    )
    @Column(name = "skill", nullable = false)
    private List<String> expertise;

    @Embedded
    private ContactDetails contactDetails;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp with time zone")
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", columnDefinition = "timestamp with time zone")
    private Instant updatedAt;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactDetails {
        @NotBlank
        @Email
        @Column(name = "contact_email", length = 320, nullable = false)
        private String email;

        @Column(name = "linkedin", length = 2048)
        private String linkedin;

        @Column(name = "website", length = 2048)
        private String website;
    }
}