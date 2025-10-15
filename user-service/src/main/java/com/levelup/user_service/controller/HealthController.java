package com.levelup.user_service.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "user-service");
        health.put("timestamp", LocalDateTime.now());
        health.put("version", "1.0.0");
        health.put("port", 8081);

        return ResponseEntity.ok(health);
    }

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "LevelUp Learning User Service");
        info.put("status", "Running");
        info.put("version", "1.0.0");
        info.put("description", "Manages user authentication, registration, and profiles");

        return ResponseEntity.ok(info);
    }
}