package com.levelup.notification_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", path = "/api/users")
public interface UserServiceClient {
    
    @GetMapping("/{userId}")
    Map<String, Object> getUserById(@PathVariable String userId);
}
