package com.levelup.assessment_service.repository;

import com.levelup.assessment_service.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    
    List<Quiz> findByLessonId(UUID lessonId);
} 