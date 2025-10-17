package com.levelup.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, String>>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "api-gateway");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return Mono.just(ResponseEntity.ok(status));
    }

    @GetMapping("/")
    public Mono<ResponseEntity<Map<String, String>>> root() {
        Map<String, String> info = new HashMap<>();
        info.put("service", "LevelUp Learning API Gateway");
        info.put("status", "Running");
        info.put("version", "1.0.0");

        return Mono.just(ResponseEntity.ok(info));
    }

    // Additional health endpoint under /api path for consistency with frontend base
    // URL
    @GetMapping("/api/health")
    public Mono<ResponseEntity<Map<String, String>>> apiHealth() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "api-gateway");
        status.put("path", "/api/health");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));

        return Mono.just(ResponseEntity.ok(status));
    }
}