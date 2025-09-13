package com.levelup.assessment_service.controller;

import com.levelup.assessment_service.dto.*;
import com.levelup.assessment_service.service.QuestionService;
import com.levelup.assessment_service.service.QuizAttemptService;
import com.levelup.assessment_service.service.QuizService;
import com.levelup.assessment_service.service.UserAnswerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessments")
@RequiredArgsConstructor
public class QuizController {
    
    private final QuizService quizService;
    private final QuestionService questionService;
    private final QuizAttemptService attemptService;
    private final UserAnswerService userAnswerService;
    
    // Quiz Management Endpoints
    
    @PostMapping("/quizzes")
    public ResponseEntity<QuizDto> createQuiz(@Valid @RequestBody CreateQuizRequest request) {
        QuizDto quiz = quizService.createQuiz(request);
        return new ResponseEntity<>(quiz, HttpStatus.CREATED);
    }
    
    @GetMapping("/quizzes/{id}")
    public ResponseEntity<QuizDto> getQuizById(@PathVariable UUID id) {
        QuizDto quiz = quizService.getQuizById(id);
        return ResponseEntity.ok(quiz);
    }
    
    @GetMapping("/quizzes/lesson/{lessonId}")
    public ResponseEntity<List<QuizDto>> getQuizzesByLesson(@PathVariable UUID lessonId) {
        List<QuizDto> quizzes = quizService.getQuizzesByLesson(lessonId);
        return ResponseEntity.ok(quizzes);
    }
    
    @PutMapping("/quizzes/{id}")
    public ResponseEntity<QuizDto> updateQuiz(@PathVariable UUID id, @Valid @RequestBody CreateQuizRequest request) {
        QuizDto quiz = quizService.updateQuiz(id, request);
        return ResponseEntity.ok(quiz);
    }
    
    @DeleteMapping("/quizzes/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable UUID id) {
        quizService.deleteQuiz(id);
        return ResponseEntity.noContent().build();
    }
    
    // Question Management Endpoints
    
    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionDto> addQuestion(@PathVariable UUID quizId, @Valid @RequestBody CreateQuestionRequest request) {
        QuestionDto question = questionService.addQuestion(quizId, request);
        return new ResponseEntity<>(question, HttpStatus.CREATED);
    }
    
    @GetMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<List<QuestionDto>> getQuestionsByQuiz(@PathVariable UUID quizId, @RequestHeader("X-User-Role") String userRole) {
        List<QuestionDto> questions = questionService.getQuestionsByQuiz(quizId, userRole);
        return ResponseEntity.ok(questions);
    }
    
    @PutMapping("/quizzes/{quizId}/questions/{questionId}")
    public ResponseEntity<QuestionDto> updateQuestion(@PathVariable UUID quizId, @PathVariable UUID questionId, 
                                                     @Valid @RequestBody CreateQuestionRequest request) {
        QuestionDto question = questionService.updateQuestion(quizId, questionId, request);
        return ResponseEntity.ok(question);
    }
    
    @DeleteMapping("/quizzes/{quizId}/questions/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable UUID quizId, @PathVariable UUID questionId) {
        questionService.deleteQuestion(quizId, questionId);
        return ResponseEntity.noContent().build();
    }
    
    // Quiz Attempt Endpoints
    
    @PostMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<QuizAttemptDto> createAttempt(@PathVariable UUID quizId, @Valid @RequestBody CreateAttemptRequest request) {
        QuizAttemptDto attempt = attemptService.createAttempt(quizId, request);
        return new ResponseEntity<>(attempt, HttpStatus.CREATED);
    }
    
    @GetMapping("/quizzes/{quizId}/attempts/{userId}")
    public ResponseEntity<List<QuizAttemptDto>> getAttemptsByUserAndQuiz(@PathVariable UUID quizId, @PathVariable UUID userId) {
        List<QuizAttemptDto> attempts = attemptService.getAttemptsByUserAndQuiz(quizId, userId);
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/attempts/user/{userId}/quiz/{quizId}")
    public ResponseEntity<List<QuizAttemptDto>> getAttemptsByUserAndQuizAlternative(@PathVariable UUID userId, @PathVariable UUID quizId) {
        List<QuizAttemptDto> attempts = attemptService.getAttemptsByUserAndQuiz(quizId, userId);
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/attempts/user/{userId}")
    public ResponseEntity<List<QuizAttemptDto>> getAttemptsByUser(@PathVariable UUID userId) {
        List<QuizAttemptDto> attempts = attemptService.getAttemptsByUser(userId);
        return ResponseEntity.ok(attempts);
    }
    
    @GetMapping("/quizzes/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptDto>> getAttemptsByQuiz(@PathVariable UUID quizId) {
        List<QuizAttemptDto> attempts = attemptService.getAttemptsByQuiz(quizId);
        return ResponseEntity.ok(attempts);
    }
    
    // User Answer Endpoints
    
    @PostMapping("/quizzes/{quizId}/attempts/{attemptId}/user-answers")
    public ResponseEntity<List<UserAnswerDto>> submitAnswers(@PathVariable UUID quizId, @PathVariable UUID attemptId, 
                                                            @Valid @RequestBody SubmitAnswersRequest request) {
        List<UserAnswerDto> answers = userAnswerService.submitAnswers(quizId, attemptId, request);
        return new ResponseEntity<>(answers, HttpStatus.CREATED);
    }
    
    @GetMapping("/quizzes/{quizId}/attempts/{attemptId}/user-answers")
    public ResponseEntity<List<UserAnswerDto>> getAnswersByAttempt(@PathVariable UUID quizId, @PathVariable UUID attemptId) {
        List<UserAnswerDto> answers = userAnswerService.getAnswersByAttempt(quizId, attemptId);
        return ResponseEntity.ok(answers);
    }
} 