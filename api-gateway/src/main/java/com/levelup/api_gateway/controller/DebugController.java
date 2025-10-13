package com.levelup.api_gateway.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/debug")
public class DebugController {

    @PostMapping("/test-auth")
    public Mono<ResponseEntity<Map<String, Object>>> testAuth(
            @RequestBody(required = false) Map<String, Object> body,
            ServerWebExchange exchange) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("method", exchange.getRequest().getMethod().toString());
        response.put("path", exchange.getRequest().getPath().toString());
        response.put("headers", exchange.getRequest().getHeaders().toSingleValueMap());
        response.put("cookies", exchange.getRequest().getCookies());
        response.put("body", body);
        response.put("remoteAddress", exchange.getRequest().getRemoteAddress());
        
        return Mono.just(ResponseEntity.ok(response));
    }

    @GetMapping("/test-get")
    public Mono<ResponseEntity<Map<String, String>>> testGet() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "GET endpoint working");
        return Mono.just(ResponseEntity.ok(response));
    }
}