package com.levelup.course_service.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import com.levelup.course_service.model.Course;

public interface CourseRepository extends MongoRepository<Course, String> {
    List<Course> findByInstructorId(String instructorId);
}