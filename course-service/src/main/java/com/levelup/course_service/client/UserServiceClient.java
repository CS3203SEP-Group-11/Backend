package com.levelup.course_service.client;

import com.levelup.course_service.dto.InstructorValidationResponseDTO;
import com.levelup.course_service.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "user-service", url = "${USER_SERVICE_URL:http://user-service:8080}")
public interface UserServiceClient {
    
    @GetMapping("/api/instructors/{userId}/validate")
    InstructorValidationResponseDTO validateInstructorByUserId(@PathVariable UUID userId);

    @GetMapping("/api/users/{userId}")
    UserDTO getUserData(@PathVariable UUID userId);
}