package com.levelup.course_service.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.levelup.course_service.service.CourseService;
import com.levelup.course_service.client.UserServiceClient;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.entity.Course;
import com.levelup.course_service.dto.CourseDTO;
import com.levelup.course_service.dto.CourseDetailsResponseDTO;
import com.levelup.course_service.dto.CourseDetailsDTO;
import com.levelup.course_service.dto.InstructorValidationResponseDTO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserServiceClient userServiceClient;

    @Override
    public Course createCourse(CourseDTO dto, UUID currentUserId) {

        // Send token's userID to user service and get validation DTO response
        InstructorValidationResponseDTO validationResponse = userServiceClient
                .validateInstructorByUserId(currentUserId);

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
    public void deleteCourse(UUID id, UUID currentUserId) {
        log.info("Deleting course {} by instructor: {}", id, currentUserId);

        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient
                .validateInstructorByUserId(currentUserId);

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
    public Course updateCourse(UUID id, CourseDTO dto, UUID currentUserId) {
        log.info("Updating course {} by instructor: {}", id, currentUserId);

        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient
                .validateInstructorByUserId(currentUserId);

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
    public String changeCourseState(UUID courseId, UUID currentUserId, String status) {
        log.info("Changing course state for course {} by instructor: {} to status: {}", courseId, currentUserId,
                status);

        // Validate instructor using user service
        InstructorValidationResponseDTO validationResponse = userServiceClient
                .validateInstructorByUserId(currentUserId);
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
    public List<Course> getMyCourses(UUID currentUserId) {
        InstructorValidationResponseDTO validationResponse = userServiceClient
                .validateInstructorByUserId(currentUserId);

        if (validationResponse.getIsValidInstructor() == null || !validationResponse.getIsValidInstructor()) {
            throw new RuntimeException("Invalid instructor: User is not a valid instructor");
        }

        return courseRepository.findByInstructorId(validationResponse.getInstructorId());
    }

    @Override
    public CourseDetailsResponseDTO getCourseDetailsByIds(List<UUID> courseIds) {
        log.info("Fetching course details for IDs: {}", courseIds);

        List<Course> courses = courseRepository.findAllById(courseIds);

        List<CourseDetailsDTO> courseDetails = courses.stream()
                .map(course -> new CourseDetailsDTO(
                        course.getId(),
                        course.getTitle(),
                        course.getPriceAmount(),
                        course.getInstructorId()))
                .collect(Collectors.toList());

        log.info("Found {} courses out of {} requested", courseDetails.size(), courseIds.size());
        return new CourseDetailsResponseDTO(courseDetails);
    }

    @Override
    public java.util.Map<String, Object> getCourseAnalytics() {
        java.util.Map<String, Object> analytics = new java.util.HashMap<>();
        
        List<Course> allCourses = courseRepository.findAll();
        
        // Basic course statistics
        analytics.put("totalCourses", allCourses.size());
        analytics.put("publishedCourses", allCourses.stream()
                .mapToInt(course -> course.getStatus() == Course.Status.PUBLISHED ? 1 : 0)
                .sum());
        
        // Average rating
        double averageRating = allCourses.stream()
                .filter(course -> course.getRatingCount() > 0 && course.getRatingAverage() != null)
                .mapToDouble(course -> course.getRatingAverage().doubleValue())
                .average()
                .orElse(0.0);
        analytics.put("averageRating", Math.round(averageRating * 10) / 10.0);
        
        // Category statistics
        java.util.Map<String, Integer> categoryEnrollments = new java.util.HashMap<>();
        int totalEnrollments = 0;
        
        for (Course course : allCourses) {
            String category = course.getCategory();
            int enrollments = course.getEnrollmentCount();
            if (category != null) {
                categoryEnrollments.put(category, categoryEnrollments.getOrDefault(category, 0) + enrollments);
                totalEnrollments += enrollments;
            }
        }
        
        // Convert to percentage format
        java.util.List<java.util.Map<String, Object>> topCategories = new java.util.ArrayList<>();
        for (java.util.Map.Entry<String, Integer> entry : categoryEnrollments.entrySet()) {
            java.util.Map<String, Object> categoryData = new java.util.HashMap<>();
            categoryData.put("name", entry.getKey());
            categoryData.put("enrollments", entry.getValue());
            categoryData.put("percentage", totalEnrollments > 0 ? 
                Math.round((entry.getValue() * 100.0 / totalEnrollments) * 10) / 10.0 : 0);
            topCategories.add(categoryData);
        }
        
        // Sort by enrollments descending
        topCategories.sort((a, b) -> Integer.compare((Integer)b.get("enrollments"), (Integer)a.get("enrollments")));
        
        analytics.put("topCategories", topCategories);
        
        return analytics;
    }
  
    public void rateCourse(UUID courseId, UUID currentUserId, int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NoSuchElementException("Course not found"));

        BigDecimal ratingAverage = course.getRatingAverage() != null ? course.getRatingAverage() : BigDecimal.ZERO;
        int ratingCount = course.getRatingCount();

        BigDecimal totalRating = ratingAverage.multiply(BigDecimal.valueOf(ratingCount))
                .add(BigDecimal.valueOf(rating));

        int newCount = ratingCount + 1;
        BigDecimal newAverage = totalRating
                .divide(BigDecimal.valueOf(newCount), 2, RoundingMode.HALF_UP);

        course.setRatingAverage(newAverage);
        course.setRatingCount(newCount);
        courseRepository.save(course);

        log.info("Course {} rated successfully: new average = {}, total ratings = {}", courseId, newAverage, newCount);
    }

}