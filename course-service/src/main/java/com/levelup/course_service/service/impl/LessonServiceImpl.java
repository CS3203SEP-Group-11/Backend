package com.levelup.course_service.service.impl;

import com.levelup.course_service.dto.LessonDTO;
import com.levelup.course_service.dto.InstructorValidationResponseDTO;
import com.levelup.course_service.entity.Lesson;
import com.levelup.course_service.entity.Course;
import com.levelup.course_service.repository.LessonRepository;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.service.LessonService;
import com.levelup.course_service.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LessonServiceImpl implements LessonService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public LessonDTO createLesson(LessonDTO dto, UUID currentUserId) {
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
    public LessonDTO updateLesson(UUID id, LessonDTO dto, UUID currentUserId) {
        log.info("Updating lesson {} by user: {}", id, currentUserId);

        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        validateUserCanModifyCourse(lesson.getCourse().getId(), currentUserId);

        if (dto.getTitle() != null)
            lesson.setTitle(dto.getTitle());
        if (dto.getContentType() != null) {
            lesson.setContentType(Lesson.ContentType.valueOf(dto.getContentType().toUpperCase()));
        }
        if (dto.getContentUrl() != null)
            lesson.setContentUrl(dto.getContentUrl());
        if (dto.getContentId() != null)
            lesson.setContentId(dto.getContentId());
        if (dto.getTextContent() != null)
            lesson.setTextContent(dto.getTextContent());
        if (dto.getQuizId() != null)
            lesson.setQuizId(dto.getQuizId());
        if (dto.getOrder() != 0)
            lesson.setOrder(dto.getOrder());
        if (dto.getStatus() != null)
            lesson.setStatus(dto.getStatus());

        lesson.setUpdatedAt(Instant.now());

        LessonDTO updatedLesson = mapToDto(lessonRepository.save(lesson));
        log.info("Lesson updated successfully: {}", updatedLesson.getId());
        return updatedLesson;
    }

    @Override
    public void deleteLesson(UUID id, UUID currentUserId) {
        log.info("Deleting lesson {} by user: {}", id, currentUserId);

        // Get lesson to find the course ID
        Lesson lesson = lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Validate that the user is an instructor and owns the course
        validateUserCanModifyCourse(lesson.getCourse().getId(), currentUserId);

        lessonRepository.deleteById(id);
        log.info("Lesson deleted successfully: {}", id);
    }

    @Override
    public LessonDTO getLessonById(UUID id) {
        return lessonRepository.findById(id).map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));
    }

    @Override
    public List<LessonDTO> getLessonsByCourse(UUID courseId) {
        return lessonRepository.findByCourseId(courseId)
                .stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Override
    public String changeLessonState(UUID lessonId, UUID currentUserId, String status) {
        log.info("Changing lesson state for {} by user: {}", lessonId, currentUserId);

        // Get lesson to find the course ID
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new RuntimeException("Lesson not found"));

        // Validate that the user is an instructor and owns the course
        validateUserCanModifyCourse(lesson.getCourse().getId(), currentUserId);

        // Update the status
        Lesson.Status newStatus = Lesson.Status.valueOf(status.toUpperCase());
        lesson.setStatus(newStatus);
        lesson.setUpdatedAt(Instant.now());

        lessonRepository.save(lesson);
        log.info("Lesson state changed successfully: {} to {}", lessonId, newStatus);
        return "Lesson state changed successfully";
    }

    private void validateUserCanModifyCourse(UUID courseId, UUID currentUserId) {
        // Step 1: Get instructor validation from user service using the user ID
        InstructorValidationResponseDTO validationResponse = userServiceClient
                .validateInstructorByUserId(currentUserId);

        // Step 2: Check if user is a valid instructor
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Access denied: User is not a valid instructor");
        }

        // Step 3: Get the instructor ID from the validation response
        UUID instructorId = validationResponse.getInstructorId();
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
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));

        return Lesson.builder()
                .course(course)
                .title(dto.getTitle())
                .contentType(Lesson.ContentType.valueOf(dto.getContentType().toUpperCase()))
                .contentUrl(dto.getContentUrl())
                .contentId(dto.getContentId())
                .textContent(dto.getTextContent())
                .status(Lesson.Status.DRAFT)
                .quizId(dto.getQuizId())
                .order(dto.getOrder())
                .build();
    }

    private Lesson mapToEntityForUpdate(LessonDTO dto) {
        // No longer used for full replacement updates because it dropped the mandatory
        // course relation.
        return Lesson.builder()
                .title(dto.getTitle())
                .contentType(
                        dto.getContentType() != null ? Lesson.ContentType.valueOf(dto.getContentType().toUpperCase())
                                : null)
                .contentUrl(dto.getContentUrl())
                .contentId(dto.getContentId())
                .textContent(dto.getTextContent())
                .quizId(dto.getQuizId())
                .order(dto.getOrder())
                .status(dto.getStatus())
                .updatedAt(Instant.now())
                .build();
    }

    private LessonDTO mapToDto(Lesson entity) {
        return LessonDTO.builder()
                .id(entity.getId())
                .courseId(entity.getCourse().getId())
                .title(entity.getTitle())
                .contentType(entity.getContentType().name())
                .contentUrl(entity.getContentUrl())
                .contentId(entity.getContentId())
                .textContent(entity.getTextContent())
                .quizId(entity.getQuizId())
                .order(entity.getOrder())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}