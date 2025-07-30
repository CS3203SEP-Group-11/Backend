package com.levelup.assessment_service.service;

import com.levelup.assessment_service.dto.CreateQuizRequest;
import com.levelup.assessment_service.dto.QuizDto;
import com.levelup.assessment_service.entity.Quiz;
import com.levelup.assessment_service.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {
    
    private final QuizRepository quizRepository;
    
    public QuizDto createQuiz(CreateQuizRequest request) {
        Quiz quiz = new Quiz();
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setLessonId(request.getLessonId());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setAttemptLimit(request.getAttemptLimit());
        quiz.setPassingScore(request.getPassingScore());
        
        Quiz savedQuiz = quizRepository.save(quiz);
        return QuizDto.fromEntity(savedQuiz);
    }
    
    @Transactional(readOnly = true)
    public QuizDto getQuizById(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        return QuizDto.fromEntity(quiz);
    }
    
    @Transactional(readOnly = true)
    public List<QuizDto> getQuizzesByLesson(UUID lessonId) {
        List<Quiz> quizzes = quizRepository.findByLessonId(lessonId);
        return quizzes.stream()
                .map(QuizDto::fromEntity)
                .collect(Collectors.toList());
    }
    
    public QuizDto updateQuiz(UUID id, CreateQuizRequest request) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        
        quiz.setTitle(request.getTitle());
        quiz.setDescription(request.getDescription());
        quiz.setLessonId(request.getLessonId());
        quiz.setTimeLimit(request.getTimeLimit());
        quiz.setAttemptLimit(request.getAttemptLimit());
        quiz.setPassingScore(request.getPassingScore());
        
        Quiz updatedQuiz = quizRepository.save(quiz);
        return QuizDto.fromEntity(updatedQuiz);
    }
    
    public void deleteQuiz(UUID id) {
        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + id));
        quizRepository.delete(quiz);
    }
} 