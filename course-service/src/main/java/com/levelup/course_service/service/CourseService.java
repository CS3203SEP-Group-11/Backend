package com.levelup.course_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.levelup.course_service.entity.Course;
import com.levelup.course_service.dto.CourseDTO;
import com.levelup.course_service.dto.CourseDetailsResponseDTO;

public interface CourseService {
    Course createCourse(CourseDTO dto, UUID currentUserId);

    List<Course> getAllCourses();

    Optional<Course> getCourseById(UUID id);

    void deleteCourse(UUID id, UUID currentUserId);

    Course updateCourse(UUID id, CourseDTO dto, UUID currentUserId);

    String changeCourseState(UUID courseId, UUID currentUserId, String status);

    List<String> getAllCategory();

    List<Course> getMyCourses(UUID currentUserId);

    CourseDetailsResponseDTO getCourseDetailsByIds(List<UUID> courseIds);
    
    java.util.Map<String, Object> getCourseAnalytics();

    void rateCourse(UUID courseId, UUID currentUserId, int rating);
}