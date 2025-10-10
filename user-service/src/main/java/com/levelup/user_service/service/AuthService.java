package com.levelup.user_service.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.levelup.user_service.dto.*;
import com.levelup.user_service.exception.*;
import com.levelup.user_service.entity.AuthProvider;
import com.levelup.user_service.entity.Role;
import com.levelup.user_service.entity.User;
import com.levelup.user_service.repository.UserRepository;
import com.levelup.user_service.security.GoogleTokenUtil;
import com.levelup.user_service.security.JwtUtil;
import com.levelup.user_service.dto.LoginRequest;
import com.levelup.user_service.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleTokenUtil googleTokenUtil;

    public String login(@Valid LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        return jwtUtil.generateJwtToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public String register(@Valid RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);

        return jwtUtil.generateJwtToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public String registerAdmin(@Valid RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(true) // Auto-verify admin emails
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.ADMIN)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);

        return jwtUtil.generateJwtToken(user.getId(), user.getEmail(), user.getRole());
    }

    public String googleLogin(GoogleAuthRequest request) {

        GoogleIdToken.Payload payload = googleTokenUtil.getTokenPayload(request.getToken());
        String email = payload.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return jwtUtil.generateJwtToken(user.getId(), user.getEmail(), user.getRole());
    }

    @Transactional
    public String googleRegister(GoogleAuthRequest request) {

        GoogleIdToken.Payload payload = googleTokenUtil.getTokenPayload(request.getToken());
        String email = payload.getEmail();

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        User user = User.builder()
                .email(email)
                .authProvider(AuthProvider.GOOGLE)
                .googleId(payload.getSubject())
                .emailVerified(true)
                .firstName((String) payload.get("given_name"))
                .lastName((String) payload.get("family_name"))
                .role(Role.USER)
                .profileImageUrl((String) payload.get("picture"))
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        userRepository.save(user);

        return jwtUtil.generateJwtToken(user.getId(), user.getEmail(), user.getRole());
    }
}