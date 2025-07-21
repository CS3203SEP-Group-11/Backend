package com.levelup.assessment_service.repository;

import com.levelup.assessment_service.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, UUID> {
    
    List<UserAnswer> findByAttemptId(UUID attemptId);
    
    List<UserAnswer> findByAttemptIdAndQuestionId(UUID attemptId, UUID questionId);
} 