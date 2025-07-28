package com.levelup.assessment_service.service;

import com.levelup.assessment_service.dto.CreateAttemptRequest;
import com.levelup.assessment_service.dto.QuizAttemptDto;
import com.levelup.assessment_service.entity.Quiz;
import com.levelup.assessment_service.entity.QuizAttempt;
import com.levelup.assessment_service.repository.QuizAttemptRepository;
import com.levelup.assessment_service.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizAttemptService {
    
    private final QuizAttemptRepository attemptRepository;
    private final QuizRepository quizRepository;
    
    public QuizAttemptDto createAttempt(UUID quizId, CreateAttemptRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
        
        // Get the next attempt number for this user and quiz
        List<QuizAttempt> existingAttempts = attemptRepository.findByQuizIdAndUserId(quizId, request.getUserId());
        int attemptNumber = existingAttempts.size() + 1;
        
        // Check if user has exceeded attempt limit
        if (attemptNumber > quiz.getAttemptLimit()) {
            throw new RuntimeException("User has exceeded the maximum number of attempts for this quiz");
        }
        
        QuizAttempt attempt = new QuizAttempt();
        attempt.setUserId(request.getUserId());
        attempt.setQuiz(quiz);
        attempt.setAttemptNumber(attemptNumber);
        attempt.setStatus(QuizAttempt.AttemptStatus.IN_PROGRESS);
        attempt.setPassed(false);
        attempt.setStartedAt(LocalDateTime.now());
        
        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        return QuizAttemptDto.fromEntity(savedAttempt);
    }
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDto> getAttemptsByUserAndQuiz(UUID quizId, UUID userId) {
        List<QuizAttempt> attempts = attemptRepository.findByQuizIdAndUserId(quizId, userId);
        return attempts.stream()
                .map(QuizAttemptDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDto> getAttemptsByUser(UUID userId) {
        List<QuizAttempt> attempts = attemptRepository.findByUserId(userId);
        return attempts.stream()
                .map(QuizAttemptDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<QuizAttemptDto> getAttemptsByQuiz(UUID quizId) {
        List<QuizAttempt> attempts = attemptRepository.findByQuizId(quizId);
        return attempts.stream()
                .map(QuizAttemptDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    public QuizAttemptDto completeAttempt(UUID attemptId, BigDecimal score, boolean passed) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found with id: " + attemptId));
        
        attempt.setScore(score);
        attempt.setPassed(passed);
        attempt.setStatus(passed ? QuizAttempt.AttemptStatus.PASSED : QuizAttempt.AttemptStatus.FAILED);
        attempt.setCompletedAt(LocalDateTime.now());
        
        // Calculate time taken in seconds
        if (attempt.getStartedAt() != null) {
            long timeTakenSeconds = java.time.Duration.between(attempt.getStartedAt(), LocalDateTime.now()).getSeconds();
            attempt.setTimeTaken((int) timeTakenSeconds);
        }
        
        QuizAttempt savedAttempt = attemptRepository.save(attempt);
        return QuizAttemptDto.fromEntity(savedAttempt);
    }
} 