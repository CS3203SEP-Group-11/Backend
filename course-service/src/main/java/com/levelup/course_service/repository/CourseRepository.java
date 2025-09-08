package com.levelup.course_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import com.levelup.course_service.model.Course;


public interface CourseRepository extends JpaRepository<Course, UUID> {
    List<Course> findByInstructorId(String instructorId);
}