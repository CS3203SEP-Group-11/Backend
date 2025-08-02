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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public Course createCourse(CourseDTO dto, String currentUserId) {
        log.info("Creating course for instructor: {}", currentUserId);
        
        // Send token's userID to user service and get validation DTO response
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructor(currentUserId);
        
        // Check if instructor is valid from DTO response
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }
        
        // Verify instructor ID matches
        if (!currentUserId.equals(validationResponse.getInstructorId())) {
            throw new RuntimeException("Instructor ID mismatch");
        }
        
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .instructorId(currentUserId)
                .category(dto.getCategory())
                .tags(dto.getTags())
                .language(dto.getLanguage())
                .thumbnailUrl(dto.getThumbnailUrl())
                .status(Course.Status.valueOf(dto.getStatus().toUpperCase()))
                .price(new Course.Price(dto.getPriceAmount(), dto.getPriceCurrency()))
                .rating(new Course.Rating(null, 0))
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
    public Optional<Course> getCourseById(String id) {
        return courseRepository.findById(id);
    }

    @Override
    public void deleteCourse(String id, String currentUserId) {
        log.info("Deleting course {} by instructor: {}", id, currentUserId);
        
        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructor(currentUserId);
        
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
    public Course updateCourse(String id, CourseDTO dto, String currentUserId) {
        log.info("Updating course {} by instructor: {}", id, currentUserId);
        
        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient.validateInstructor(currentUserId);
        
        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }
        
        return courseRepository.findById(id).map(course -> {
            // Validate that instructor owns this course
            if (!course.getInstructorId().equals(currentUserId)) {
                throw new RuntimeException("You can only update courses that you own");
            }
            
            course.setTitle(dto.getTitle());
            course.setDescription(dto.getDescription());
            course.setCategory(dto.getCategory());
            course.setTags(dto.getTags());
            course.setLanguage(dto.getLanguage());
            course.setThumbnailUrl(dto.getThumbnailUrl());
            course.setStatus(Course.Status.valueOf(dto.getStatus().toUpperCase()));
            course.setPrice(new Course.Price(dto.getPriceAmount(), dto.getPriceCurrency()));
            course.setUpdatedAt(Instant.now());
            
            Course updatedCourse = courseRepository.save(course);
            log.info("Course updated successfully: {}", updatedCourse.getId());
            return updatedCourse;
            
        }).orElseThrow(() -> new RuntimeException("Course not found"));
    }
}