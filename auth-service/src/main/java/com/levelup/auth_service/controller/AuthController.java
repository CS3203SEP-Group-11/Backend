package com.levelup.auth_service.controller;

import com.levelup.auth_service.dto.GoogleAuthRequest;
import com.levelup.auth_service.dto.LoginRequest;
import com.levelup.auth_service.dto.RegisterRequest;
import com.levelup.auth_service.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        String jwtToken = authService.login(request);
        addJwtToCookie(response, jwtToken);
        return ResponseEntity.ok("Login successful");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response) {
        String jwtToken = authService.register(request);
        addJwtToCookie(response, jwtToken);
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/google/login")
    public ResponseEntity<String> googleLogin(@RequestBody GoogleAuthRequest request, HttpServletResponse response) {
        String jwtToken = authService.googleLogin(request);
        addJwtToCookie(response, jwtToken);
        return ResponseEntity.ok("Google login successful");
    }

    @PostMapping("/google/register")
    public ResponseEntity<String> googleRegister(@RequestBody GoogleAuthRequest request, HttpServletResponse response) {
        String jwtToken = authService.googleRegister(request);
        addJwtToCookie(response, jwtToken);
        return ResponseEntity.ok("Google registration successful");
    }


    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false) // true in production with HTTPS
                .path("/")
                .maxAge(0) // Expire the cookie
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok("Logout successful");
    }

    private void addJwtToCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // true in production with HTTPS
                .path("/")
                .maxAge(60 * 60 * 24) // 1 day
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
