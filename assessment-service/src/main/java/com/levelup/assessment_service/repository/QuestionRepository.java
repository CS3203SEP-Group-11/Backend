package com.levelup.assessment_service.repository;

import com.levelup.assessment_service.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    
    List<Question> findByQuizId(UUID quizId);
    
    List<Question> findByQuizIdOrderByOrderAsc(UUID quizId);
} 