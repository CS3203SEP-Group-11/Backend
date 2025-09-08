package com.levelup.course_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.levelup.course_service.service.CourseService;
import com.levelup.course_service.client.UserServiceClient;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.dto.CourseDTO;
import com.levelup.course_service.dto.InstructorValidationResponseDTO;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public Course createCourse(CourseDTO dto, String currentUserId) {
        
        // Send token's userID to user service and get validation DTO response
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructorByUserId(currentUserId);
        
        // Check if instructor is valid from DTO response
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }

        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .instructorId(validationResponse.getInstructorId())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .language(dto.getLanguage())
                .thumbnailUrl(dto.getThumbnailUrl())
                .thumbnailId(dto.getThumbnailId()) // Optional, if using cloud storage
                .status(Course.Status.DRAFT)
                .priceAmount(dto.getPriceAmount())
                .priceCurrency(dto.getPriceCurrency())
                .ratingAverage(BigDecimal.valueOf(0.0))
                .ratingCount(0)
                .duration(dto.getDuration())
                .level(dto.getLevel())
                .enrollmentCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
                
        Course savedCourse = courseRepository.save(course);
        log.info("Course created successfully by instructor {}: {}", currentUserId, savedCourse.getId());
        return savedCourse;
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> getCourseById(UUID id) {
        return courseRepository.findById(id);
    }

    @Override
    public void deleteCourse(UUID id, String currentUserId) {
        log.info("Deleting course {} by instructor: {}", id, currentUserId);
        
        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructorByUserId(currentUserId);
        
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }
        
        // Get the course to check ownership
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Validate that instructor owns this course
        if (!course.getInstructorId().equals(currentUserId)) {
            throw new RuntimeException("You can only delete courses that you own");
        }
        
        courseRepository.deleteById(id);
        log.info("Course deleted successfully: {}", id);
    }

    @Override
    public Course updateCourse(UUID id, CourseDTO dto, String currentUserId) {
        log.info("Updating course {} by instructor: {}", id, currentUserId);
        
        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructorByUserId(currentUserId);
        
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }
        
        return courseRepository.findById(id).map(course -> {
            // Validate that instructor owns this course
            if (!course.getInstructorId().equals(validationResponse.getInstructorId())) {
                throw new RuntimeException("You can only update courses that you own");
            }
            
            course.setTitle(dto.getTitle());
            course.setDescription(dto.getDescription());
            course.setCategory(dto.getCategory());
            course.setTags(dto.getTags());
            course.setLanguage(dto.getLanguage());
            course.setThumbnailUrl(dto.getThumbnailUrl());
            course.setThumbnailId(dto.getThumbnailId()); // Optional, if using cloud storage
            course.setPriceAmount(dto.getPriceAmount());
            course.setPriceCurrency(dto.getPriceCurrency());
            course.setDuration(dto.getDuration());
            course.setLevel(dto.getLevel());
            course.setUpdatedAt(Instant.now());
            
            Course updatedCourse = courseRepository.save(course);
            log.info("Course updated successfully: {}", updatedCourse.getId());
            return updatedCourse;
            
        }).orElseThrow(() -> new RuntimeException("Course not found"));
    }

    @Override
    public String changeCourseState(UUID courseId, String currentUserId, String status) {
        log.info("Changing course state for course {} by instructor: {} to status: {}", courseId, currentUserId, status);

        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructorByUserId(currentUserId);
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new RuntimeException("Course not found"));



        // Validate and set new status
        try {
            Course.Status newStatus = Course.Status.valueOf(status.toUpperCase());
            course.setStatus(newStatus);
            courseRepository.save(course);
            log.info("Course state changed successfully: {} -> {}", courseId, newStatus);
            return "Course state changed to " + newStatus;
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid course status: " + status);
        }
    }

    @Override
    public List<String> getAllCategory() {
        return courseRepository.findAll().stream()
                .map(Course::getCategory)
                .distinct()
                .sorted()
                .toList();
    }

    @Override
    public List<Course> getMyCourses(String currentUserId) {
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructorByUserId(currentUserId);

        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }

        return courseRepository.findByInstructorId(validationResponse.getInstructorId());
    }
}