package com.levelup.course_service.repository;

import com.levelup.course_service.entity.CourseEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, UUID> {
    List<CourseEnrollment> findByUserId(UUID userId);

    List<CourseEnrollment> findByCourseId(UUID courseId);

    boolean existsByUserIdAndCourseId(UUID userId, UUID courseId);

    Optional<CourseEnrollment> findByUserIdAndCourseId(UUID userId, UUID courseId);

    int countByCourseId(UUID courseId);
}
