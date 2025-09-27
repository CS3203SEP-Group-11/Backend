package com.levelup.payment_service.client;

import com.levelup.payment_service.dto.external.CourseServiceRequest;
import com.levelup.payment_service.dto.external.CourseServiceResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${course-service.base-url}")
    private String courseServiceBaseUrl;

    public CourseServiceResponse getCourseDetails(CourseServiceRequest request) {
        try {
            log.info("Fetching course details for course IDs: {}", request.getCourseIds());

            return webClientBuilder.build()
                    .post()
                    .uri(courseServiceBaseUrl + "/api/courses/details")
                    .body(Mono.just(request), CourseServiceRequest.class)
                    .retrieve()
                    .bodyToMono(CourseServiceResponse.class)
                    .block();
        } catch (Exception e) {
            log.error("Error fetching course details: ", e);
            throw new RuntimeException("Failed to fetch course details", e);
        }
    }
}