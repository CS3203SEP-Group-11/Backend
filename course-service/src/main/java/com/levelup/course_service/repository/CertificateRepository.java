package com.levelup.course_service.repository;

import com.levelup.course_service.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository extends JpaRepository<Certificate, UUID> {
    boolean existsByEnrollmentId(UUID enrollmentId);
    Optional<Certificate> findByEnrollmentId(UUID enrollmentId);
}
