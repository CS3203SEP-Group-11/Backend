package com.levelup.assessment_service.service;

import com.levelup.assessment_service.dto.CreateQuestionRequest;
import com.levelup.assessment_service.dto.QuestionDto;
import com.levelup.assessment_service.dto.QuestionOptionDto;
import com.levelup.assessment_service.entity.Question;
import com.levelup.assessment_service.entity.QuestionOption;
import com.levelup.assessment_service.entity.Quiz;
import com.levelup.assessment_service.repository.QuestionOptionRepository;
import com.levelup.assessment_service.repository.QuestionRepository;
import com.levelup.assessment_service.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class QuestionService {
    
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final QuizRepository quizRepository;
    
    public QuestionDto addQuestion(UUID quizId, CreateQuestionRequest request) {
        log.info("{}, {}, {}", request.getOptions(), request.getQuestionText(), request.getOrder());
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quizId));
        
        Question question = new Question();
        question.setQuestionText(request.getQuestionText());
        question.setOrder(request.getOrder());
        question.setQuiz(quiz);
        
        Question savedQuestion = questionRepository.save(question);
        
        // Save options if provided
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            List<QuestionOption> options = request.getOptions().stream()
                    .map(optionDto -> {
                        QuestionOption option = new QuestionOption();
                        option.setOptionText(optionDto.getOptionText());
                        option.setIsCorrect(optionDto.getIsCorrect());
                        option.setQuestion(savedQuestion);
                        return option;
                    })
                    .collect(Collectors.toList());
            
            optionRepository.saveAll(options);
        }
        
        return QuestionDto.fromEntity(savedQuestion);
    }

    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestionsByQuiz(UUID quizId, String userRole) {

        boolean showCorrectAnswers = "ADMIN".equalsIgnoreCase(userRole) || "INSTRUCTOR".equalsIgnoreCase(userRole);

        List<Question> questions = questionRepository.findByQuizIdWithOptions(quizId);
        return questions.stream()
                .map(question -> convertToDto(question, showCorrectAnswers))
                .collect(Collectors.toList());
    }

    private QuestionDto convertToDto(Question question, boolean showCorrectAnswers) {
        QuestionDto dto = QuestionDto.fromEntity(question);

        // Convert options to DTOs
        if (question.getOptions() != null) {
            List<QuestionOptionDto> optionDtos = question.getOptions().stream()
                    .map(option -> convertOptionToDto(option, showCorrectAnswers))
                    .collect(Collectors.toList());
            dto.setOptions(optionDtos);
        }
        return dto;
    }

    private QuestionOptionDto convertOptionToDto(QuestionOption option, boolean showCorrectAnswers) {
        QuestionOptionDto dto = new QuestionOptionDto();
        dto.setId(option.getId());
        dto.setOptionText(option.getOptionText());
        dto.setCreatedAt(option.getCreatedAt());
        dto.setUpdatedAt(option.getUpdatedAt());
        if (showCorrectAnswers) {
            dto.setIsCorrect(option.getIsCorrect());
        }
        return dto;
    }
    
    public QuestionDto updateQuestion(UUID quizId, UUID questionId, CreateQuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        
        // Verify the question belongs to the specified quiz
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Question does not belong to the specified quiz");
        }
        
        question.setQuestionText(request.getQuestionText());
        question.setOrder(request.getOrder());
        
        Question updatedQuestion = questionRepository.save(question);
        
        // Update options if provided
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            // Delete existing options
            optionRepository.deleteByQuestionId(questionId);
            
            // Save new options
            List<QuestionOption> options = request.getOptions().stream()
                    .map(optionDto -> {
                        QuestionOption option = new QuestionOption();
                        option.setOptionText(optionDto.getOptionText());
                        option.setIsCorrect(optionDto.getIsCorrect());
                        option.setQuestion(updatedQuestion);
                        return option;
                    })
                    .collect(Collectors.toList());
            
            optionRepository.saveAll(options);
        }
        
        return QuestionDto.fromEntity(updatedQuestion);
    }
    
    public void deleteQuestion(UUID quizId, UUID questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + questionId));
        
        // Verify the question belongs to the specified quiz
        if (!question.getQuiz().getId().equals(quizId)) {
            throw new RuntimeException("Question does not belong to the specified quiz");
        }
        
        questionRepository.delete(question);
    }
} 