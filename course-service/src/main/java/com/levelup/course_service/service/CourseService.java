package com.levelup.course_service.service;

import java.util.List;
import java.util.Optional;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.dto.CourseDTO;

public interface CourseService {
    Course createCourse(CourseDTO dto, String currentUserId);
    List<Course> getAllCourses();
    Optional<Course> getCourseById(String id);
    void deleteCourse(String id, String currentUserId);
    Course updateCourse(String id, CourseDTO dto, String currentUserId);
    String changeCourseState(String courseId, String currentUserId, String status);
    List<String> getAllCategory();
    List<Course> getMyCourses(String currentUserId);
}