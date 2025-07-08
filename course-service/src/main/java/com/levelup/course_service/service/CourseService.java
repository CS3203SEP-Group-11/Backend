package com.levelup.course_service.service;

import java.util.List;
import java.util.Optional;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.dto.CourseDTO;

public interface CourseService {
    Course createCourse(CourseDTO dto);
    List<Course> getAllCourses();
    Optional<Course> getCourseById(String id);
    void deleteCourse(String id);
    Course updateCourse(String id, CourseDTO dto);
}