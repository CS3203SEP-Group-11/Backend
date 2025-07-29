package com.levelup.course_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.levelup.course_service.service.CourseService;
import com.levelup.course_service.service.InstructorValidationService;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.dto.CourseDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final InstructorValidationService instructorValidationService;

    @Override
    public Course createCourse(CourseDTO dto, String currentUserId) {
        log.info("Creating course for instructor: {}", currentUserId);
        
        // Validate that current user is a valid instructor (boolean response)
        instructorValidationService.validateInstructor(currentUserId);
        
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
        
        // Validate that current user is a valid instructor
        instructorValidationService.validateInstructor(currentUserId);
        
        // Get the course to check ownership
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Validate that instructor owns this course
        instructorValidationService.validateInstructorOwnership(course.getInstructorId(), currentUserId);
        
        courseRepository.deleteById(id);
        log.info("Course deleted successfully: {}", id);
    }

    @Override
    public Course updateCourse(String id, CourseDTO dto, String currentUserId) {
        log.info("Updating course {} by instructor: {}", id, currentUserId);
        
        // Validate that current user is a valid instructor
        instructorValidationService.validateInstructor(currentUserId);
        
        return courseRepository.findById(id).map(course -> {
            // Validate that instructor owns this course
            instructorValidationService.validateInstructorOwnership(course.getInstructorId(), currentUserId);
            
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