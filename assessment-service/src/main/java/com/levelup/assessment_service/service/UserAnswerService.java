package com.levelup.assessment_service.service;

import com.levelup.assessment_service.dto.SubmitAnswersRequest;
import com.levelup.assessment_service.dto.UserAnswerDto;
import com.levelup.assessment_service.entity.*;
import com.levelup.assessment_service.repository.QuestionOptionRepository;
import com.levelup.assessment_service.repository.QuestionRepository;
import com.levelup.assessment_service.repository.QuizAttemptRepository;
import com.levelup.assessment_service.repository.UserAnswerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserAnswerService {
    
    private final UserAnswerRepository userAnswerRepository;
    private final QuizAttemptRepository attemptRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final QuizAttemptService attemptService;
    
    public List<UserAnswerDto> submitAnswers(UUID quizId, UUID attemptId, SubmitAnswersRequest request) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found with id: " + attemptId));
        
        // Verify the attempt belongs to the specified quiz
        if (!attempt.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Attempt does not belong to the specified quiz");
        }
        
        // Check if attempt is still in progress
        if (attempt.getStatus() != QuizAttempt.AttemptStatus.IN_PROGRESS) {
            throw new RuntimeException("Cannot submit answers for a completed attempt");
        }
        
        List<UserAnswer> userAnswers = request.getAnswers().stream()
                .map(answerSubmission -> {
                    Question question = questionRepository.findById(answerSubmission.getQuestionId())
                            .orElseThrow(() -> new RuntimeException("Question not found with id: " + answerSubmission.getQuestionId()));
                    
                    UserAnswer userAnswer = new UserAnswer();
                    userAnswer.setAttempt(attempt);
                    userAnswer.setQuestion(question);
                    userAnswer.setSelectedOptionId(answerSubmission.getSelectedOptionId());
                    
                    // Check if answer is correct
                    boolean isCorrect = false;
                    if (answerSubmission.getSelectedOptionId() != null) {
                        QuestionOption selectedOption = optionRepository.findById(answerSubmission.getSelectedOptionId())
                                .orElse(null);
                        isCorrect = selectedOption != null && selectedOption.getIsCorrect();
                    }
                    
                    userAnswer.setIsCorrect(isCorrect);
                    return userAnswer;
                })
                .collect(Collectors.toList());

        List<Question> allQuestions = questionRepository.findByQuizId(quizId);
        int totalQuestions = allQuestions.size();
        List<UserAnswer> savedAnswers = userAnswerRepository.saveAll(userAnswers);
        
        // Calculate total score and determine if passed
        long correctAnswers = savedAnswers.stream().filter(UserAnswer::getIsCorrect).count();
        BigDecimal score = BigDecimal.valueOf(correctAnswers/ (double) totalQuestions * 100);
        boolean passed = score.compareTo(attempt.getQuiz().getPassingScore()) >= 0;
        
        // Complete the attempt
        attemptService.completeAttempt(attemptId, score, passed);
        
        return savedAnswers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<UserAnswerDto> getAnswersByAttempt(UUID quizId, UUID attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found with id: " + attemptId));
        
        // Verify the attempt belongs to the specified quiz
        if (!attempt.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Attempt does not belong to the specified quiz");
        }
        
        List<UserAnswer> userAnswers = userAnswerRepository.findByAttemptId(attemptId);
        return userAnswers.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private UserAnswerDto convertToDto(UserAnswer userAnswer) {
        UserAnswerDto dto = new UserAnswerDto();
        dto.setId(userAnswer.getId());
        dto.setQuestionId(userAnswer.getQuestion().getId());
        dto.setSelectedOptionId(userAnswer.getSelectedOptionId());
        dto.setIsCorrect(userAnswer.getIsCorrect());
        dto.setCreatedAt(userAnswer.getCreatedAt());
        dto.setUpdatedAt(userAnswer.getUpdatedAt());
        return dto;
    }
} 