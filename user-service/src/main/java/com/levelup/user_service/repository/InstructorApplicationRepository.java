package com.levelup.user_service.repository;

import com.levelup.user_service.entity.ApplicationStatus;
import com.levelup.user_service.entity.InstructorApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, UUID> {
    List<InstructorApplication> findByStatus(ApplicationStatus status);
    Optional<InstructorApplication> findTopByUser_IdOrderByCreatedAtDesc(UUID userId);
}
