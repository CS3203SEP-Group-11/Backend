package com.levelup.user_service.repository;

import com.levelup.user_service.entity.Instructor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstructorRepository extends JpaRepository<Instructor, UUID> {
    boolean existsByUserId(UUID userId);
    Optional<Instructor> findByUserId(UUID instructorId);
}