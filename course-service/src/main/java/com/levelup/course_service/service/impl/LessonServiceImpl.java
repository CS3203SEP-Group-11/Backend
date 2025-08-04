package com.levelup.course_service.service.impl;

import com.levelup.course_service.dto.LessonDTO;
import com.levelup.course_service.dto.InstructorValidationResponseDTO;
import com.levelup.course_service.model.Lesson;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.repository.LessonRepository;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.service.LessonService;
import com.levelup.course_service.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public LessonDTO createLesson(LessonDTO dto, String currentUserId) {
        log.info("Creating lesson for course {} by user: {}", dto.getCourseId(), currentUserId);
        
        // Validate that the user is an instructor and owns the course
        validateUserCanModifyCourse(dto.getCourseId(), currentUserId);
        
        Lesson lesson = mapToEntityForCreate(dto);
        lesson.setStatus(Lesson.Status.DRAFT);
        lesson.setCreatedAt(Instant.now());
        lesson.setUpdatedAt(Instant.now());
        
        LessonDTO createdLesson = mapToDto(lessonRepository.save(lesson));
        log.info("Lesson created successfully: {}", createdLesson.getId());
        return createdLesson;
    }

    @Override
    public LessonDTO updateLesson(String id, LessonDTO dto, String currentUserId) {
        log.info("Updating lesson {} by user: {}", id, currentUserId);
        
        // Validate that the user is an instructor and owns the course
        validateUserCanModifyCourse(dto.getCourseId(), currentUserId);
        
        Lesson existing = lessonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
            
        Lesson updated = mapToEntityForUpdate(dto);
        updated.setId(id);
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        
        LessonDTO updatedLesson = mapToDto(lessonRepository.save(updated));
        log.info("Lesson updated successfully: {}", updatedLesson.getId());
        return updatedLesson;
    }

    @Override
    public void deleteLesson(String id, String currentUserId) {
        log.info("Deleting lesson {} by user: {}", id, currentUserId);
        
        // Get lesson to find the course ID
        Lesson lesson = lessonRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
            
        // Validate that the user is an instructor and owns the course
        validateUserCanModifyCourse(lesson.getCourseId(), currentUserId);
        
        lessonRepository.deleteById(id);
        log.info("Lesson deleted successfully: {}", id);
    }

    @Override
    public LessonDTO getLessonById(String id) {
        return lessonRepository.findById(id).map(this::mapToDto)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    @Override
    public List<LessonDTO> getLessonsByCourse(String courseId) {
        return lessonRepository.findByCourseId(courseId)
            .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public String changeLessonState(String lessonId, String currentUserId, String status) {
        log.info("Changing lesson state for {} by user: {}", lessonId, currentUserId);

        // Get lesson to find the course ID
        Lesson lesson = lessonRepository.findById(lessonId)
            .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Validate that the user is an instructor and owns the course
        validateUserCanModifyCourse(lesson.getCourseId(), currentUserId);

        // Update the status
        Lesson.Status newStatus = Lesson.Status.valueOf(status.toUpperCase());
        lesson.setStatus(newStatus);
        lesson.setUpdatedAt(Instant.now());

        lessonRepository.save(lesson);
        log.info("Lesson state changed successfully: {} to {}", lessonId, newStatus);
        return "Lesson state changed successfully";
    }

    private void validateUserCanModifyCourse(String courseId, String currentUserId) {
        // Step 1: Get instructor validation from user service using the user ID
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructor(currentUserId);
        
        // Step 2: Check if user is a valid instructor
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Access denied: User is not a valid instructor");
        }
        
        // Step 3: Get the instructor ID from the validation response
        String instructorId = validationResponse.getInstructorId();
        log.info("User {} validated as instructor with ID: {}", currentUserId, instructorId);
        
        // Step 4: Get the course to check ownership
        Course course = courseRepository.findById(courseId)
            .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Step 5: Check if the instructor ID matches the course's instructor ID
        if (!course.getInstructorId().equals(instructorId)) {
            throw new RuntimeException("Access denied: You can only modify lessons for courses that you own");
        }
        
        log.info("Access granted: Instructor {} can modify course {}", instructorId, courseId);
    }



    private Lesson mapToEntityForCreate(LessonDTO dto) {
        return Lesson.builder()
                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .contentType(Lesson.ContentType.valueOf(dto.getContentType().toUpperCase()))
                .contentUrl(dto.getContentUrl().stream()
                        .map(url -> new Lesson.ContentUrl(url.getType(), url.getUrl()))
                        .collect(Collectors.toList()))
                .textContent(dto.getTextContent())
                .status(Lesson.Status.DRAFT)
                .quizId(dto.getQuizId())
                .order(dto.getOrder())
                .build();
    }

    private Lesson mapToEntityForUpdate(LessonDTO dto) {
        return Lesson.builder()

                .courseId(dto.getCourseId())
                .title(dto.getTitle())
                .contentType(Lesson.ContentType.valueOf(dto.getContentType().toUpperCase()))
                .contentUrl(dto.getContentUrl().stream()
                        .map(url -> new Lesson.ContentUrl(url.getType(), url.getUrl()))
                        .collect(Collectors.toList()))
                .textContent(dto.getTextContent())
                .quizId(dto.getQuizId())
                .order(dto.getOrder())

                .updatedAt(Instant.now())
                .build();
    }

    private LessonDTO mapToDto(Lesson entity) {
        return LessonDTO.builder()
            .id(entity.getId())
            .courseId(entity.getCourseId())
            .title(entity.getTitle())
            .contentType(entity.getContentType().name())
            .contentUrl(entity.getContentUrl().stream()
                .map(url -> new LessonDTO.ContentUrlDto(url.getType(), url.getUrl()))
                .collect(Collectors.toList()))
            .textContent(entity.getTextContent())
            .quizId(entity.getQuizId())
            .order(entity.getOrder())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .updatedAt(entity.getUpdatedAt())
            .build();
    }
}