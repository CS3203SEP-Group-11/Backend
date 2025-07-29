package com.levelup.course_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", path = "/api/instructors")
public interface UserServiceClient {
    
    @GetMapping("/{instructorId}/validate")
    Boolean validateInstructor(@PathVariable String instructorId);
}