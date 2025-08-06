package com.levelup.user_service.repository;

import com.levelup.user_service.model.Instructor;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface InstructorRepository extends MongoRepository<Instructor, String> {
    boolean existsByUserId(String userId);
    Optional<Instructor> findByUserId(String instructorId);
}
