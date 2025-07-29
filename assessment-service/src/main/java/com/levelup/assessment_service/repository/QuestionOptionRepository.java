package com.levelup.assessment_service.repository;

import com.levelup.assessment_service.entity.QuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionOptionRepository extends JpaRepository<QuestionOption, UUID> {
    
    List<QuestionOption> findByQuestionId(UUID questionId);
    
    List<QuestionOption> findByQuestionIdOrderByCreatedAtAsc(UUID questionId);
    
    @Modifying
    @Query("DELETE FROM QuestionOption o WHERE o.question.id = :questionId")
    void deleteByQuestionId(UUID questionId);
} 