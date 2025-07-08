package com.levelup.course_service.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.levelup.course_service.service.CourseService;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.model.Course;
import com.levelup.course_service.dto.CourseDTO;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;

    @Override
    public Course createCourse(CourseDTO dto) {
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .instructorId(dto.getInstructorId())
                .category(dto.getCategory())
                .tags(dto.getTags())
                .language(dto.getLanguage())
                .thumbnailUrl(dto.getThumbnailUrl())
                .status(Course.Status.valueOf(dto.getStatus().toUpperCase()))
                .price(new Course.Price(dto.getPriceAmount(), dto.getPriceCurrency()))
                .rating(new Course.Rating(null, 0))
                .enrollmentCount(0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        return courseRepository.save(course);
    }

    @Override
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Override
    public Optional<Course> getCourseById(String id) {
        return courseRepository.findById(id);
    }

    @Override
    public void deleteCourse(String id) {
        courseRepository.deleteById(id);
    }

    @Override
    public Course updateCourse(String id, CourseDTO dto) {
        return courseRepository.findById(id).map(course -> {
            course.setTitle(dto.getTitle());
            course.setDescription(dto.getDescription());
            course.setCategory(dto.getCategory());
            course.setTags(dto.getTags());
            course.setLanguage(dto.getLanguage());
            course.setThumbnailUrl(dto.getThumbnailUrl());
            course.setStatus(Course.Status.valueOf(dto.getStatus().toUpperCase()));
            course.setPrice(new Course.Price(dto.getPriceAmount(), dto.getPriceCurrency()));
            course.setUpdatedAt(Instant.now());
            return courseRepository.save(course);
        }).orElseThrow(() -> new RuntimeException("Course not found"));
    }
}