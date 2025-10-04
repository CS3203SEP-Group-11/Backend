package com.levelup.course_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
        name = "course_certificates",
        uniqueConstraints = @UniqueConstraint(name = "uk_certificate_enrollment", columnNames = "enrollment_id")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false, foreignKey = @ForeignKey(name = "fk_certificate_enrollment"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private CourseEnrollment enrollment;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "certificate_url", nullable = false)
    private String certificateUrl;
}