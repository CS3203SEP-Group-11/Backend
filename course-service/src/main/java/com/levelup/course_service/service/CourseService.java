package com.levelup.course_service.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.levelup.course_service.model.Course;
import com.levelup.course_service.dto.CourseDTO;

public interface CourseService {
    Course createCourse(CourseDTO dto, String currentUserId);
    List<Course> getAllCourses();
    Optional<Course> getCourseById(UUID id);
    void deleteCourse(UUID id, String currentUserId);
    Course updateCourse(UUID id, CourseDTO dto, String currentUserId);
    String changeCourseState(UUID courseId, String currentUserId, String status);
    List<String> getAllCategory();
    List<Course> getMyCourses(String currentUserId);
}