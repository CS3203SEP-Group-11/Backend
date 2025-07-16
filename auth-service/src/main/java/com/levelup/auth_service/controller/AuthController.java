package com.levelup.auth_service.controller;

import com.levelup.auth_service.dto.AuthResponse;
import com.levelup.auth_service.dto.LoginRequest;
import com.levelup.auth_service.dto.RegisterRequest;
import com.levelup.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/google")
    public ResponseEntity<Void> redirectToGoogleOAuth() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("/oauth2/authorization/google"))
                .build();
    }

    @GetMapping("/google/success")
    public ResponseEntity<String> handleGoogleOAuthCallback(OAuth2AuthenticationToken authenticationToken ) throws IOException {
        AuthResponse authResponse = authService.processOAuth2Login(authenticationToken);
        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create("http://localhost:3000/home?token=" + authResponse.getToken()))
                .build();
    }
}
