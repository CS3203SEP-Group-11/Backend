package com.levelup.paymentservice.client;

import com.levelup.paymentservice.dto.CourseDto;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
public class CourseServiceClient {

    private final WebClient webClient;

    public CourseServiceClient(@Qualifier("courseServiceWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<CourseDto> getCourseById(UUID courseId) {
        return webClient
                .get()
                .uri("/api/courses/{courseId}", courseId)
                .retrieve()
                .bodyToMono(CourseDto.class)
                .onErrorReturn(new CourseDto()); // Return empty DTO on error
    }

    public Flux<CourseDto> getCoursesByIds(List<UUID> courseIds) {
        return webClient
                .post()
                .uri("/api/courses/batch")
                .bodyValue(courseIds)
                .retrieve()
                .bodyToFlux(CourseDto.class);
    }

    public Mono<Boolean> validateCourseExists(UUID courseId) {
        return webClient
                .get()
                .uri("/api/courses/{courseId}/exists", courseId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    public Mono<Boolean> validateCoursesExist(List<UUID> courseIds) {
        return webClient
                .post()
                .uri("/api/courses/validate")
                .bodyValue(courseIds)
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    public Mono<Boolean> grantCourseAccess(UUID userId, UUID courseId) {
        return webClient
                .post()
                .uri("/api/courses/{courseId}/access/grant", courseId)
                .bodyValue(new CourseAccessRequest(userId))
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    public Mono<Boolean> revokeCourseAccess(UUID userId, UUID courseId) {
        return webClient
                .post()
                .uri("/api/courses/{courseId}/access/revoke", courseId)
                .bodyValue(new CourseAccessRequest(userId))
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    public Mono<Boolean> grantBulkCourseAccess(UUID userId, List<UUID> courseIds) {
        return webClient
                .post()
                .uri("/api/courses/access/grant-bulk")
                .bodyValue(new BulkCourseAccessRequest(userId, courseIds))
                .retrieve()
                .bodyToMono(Boolean.class)
                .onErrorReturn(false);
    }

    // Inner classes for requests
    public static class CourseAccessRequest {
        private UUID userId;

        public CourseAccessRequest() {
        }

        public CourseAccessRequest(UUID userId) {
            this.userId = userId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }
    }

    public static class BulkCourseAccessRequest {
        private UUID userId;
        private List<UUID> courseIds;

        public BulkCourseAccessRequest() {
        }

        public BulkCourseAccessRequest(UUID userId, List<UUID> courseIds) {
            this.userId = userId;
            this.courseIds = courseIds;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        public List<UUID> getCourseIds() {
            return courseIds;
        }

        public void setCourseIds(List<UUID> courseIds) {
            this.courseIds = courseIds;
        }
    }
}
