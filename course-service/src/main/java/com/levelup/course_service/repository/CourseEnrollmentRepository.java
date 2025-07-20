package com.levelup.course_service.repository;

import com.levelup.course_service.model.CourseEnrollment;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CourseEnrollmentRepository extends MongoRepository<CourseEnrollment, String> {
    List<CourseEnrollment> findByUserId(String userId);
    List<CourseEnrollment> findByCourseId(String courseId);
    boolean existsByUserIdAndCourseId(String userId, String courseId);
}
