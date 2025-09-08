package com.levelup.course_service.repository;

import com.levelup.course_service.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {
    List<CourseEnrollment> findByUserId(String userId);
    List<CourseEnrollment> findByCourseId(UUID courseId);
    boolean existsByUserIdAndCourseId(String userId, UUID courseId);
}
