package com.levelup.auth_service.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.levelup.auth_service.dto.*;
import com.levelup.auth_service.exception.*;
import com.levelup.auth_service.model.AuthProvider;
import com.levelup.auth_service.model.AuthUser;
import com.levelup.auth_service.repository.AuthRepository;
import com.levelup.auth_service.security.GoogleTokenUtil;
import com.levelup.auth_service.security.JwtUtil;
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

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final UserIntegrationService userIntegrationService;
    private final GoogleTokenUtil googleTokenUtil;

    public String login(LoginRequest request) {
        AuthUser authUser = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        UserDTO user = userIntegrationService.getUserById(authUser.getUserId());

        return jwtUtil.generateJwtToken(authUser.getUserId(), authUser.getEmail(), user.getRole());
    }

    @Transactional
    public String register(RegisterRequest request) {
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        AuthUser authUser = AuthUser.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .authProvider(AuthProvider.LOCAL)
                .emailVerified(false)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        authUser = authRepository.save(authUser);

        UserDTO user = UserDTO.builder()
                .id(authUser.getUserId())
                .email(request.getEmail())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(Role.USER)
                .build();

        try {
            userIntegrationService.createUser(user);
        } catch (Exception ex) {
            authRepository.deleteById(authUser.getUserId());
            throw new AuthException("User creation failed. Rolled back auth record.");
        }

        return jwtUtil.generateJwtToken(authUser.getUserId(), authUser.getEmail(), user.getRole());
    }

    public String googleLogin(GoogleAuthRequest request) {

        GoogleIdToken.Payload payload = googleTokenUtil.getTokenPayload(request.getToken());
        String email = payload.getEmail();

        AuthUser authUser = authRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        UserDTO user = userIntegrationService.getUserById(authUser.getUserId());

        return jwtUtil.generateJwtToken(authUser.getUserId(), authUser.getEmail(), user.getRole());
    }

    @Transactional
    public String googleRegister(GoogleAuthRequest request) {

        GoogleIdToken.Payload payload = googleTokenUtil.getTokenPayload(request.getToken());
        String email = payload.getEmail();

        if (authRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException("Email already exists");
        }

        AuthUser authUser = AuthUser.builder()
                .email(email)
                .authProvider(AuthProvider.GOOGLE)
                .googleId(payload.getSubject())
                .emailVerified(true)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        authUser = authRepository.save(authUser);

        UserDTO user = UserDTO.builder()
                .id(authUser.getUserId())
                .email(email)
                .firstName((String) payload.get("given_name"))
                .lastName((String) payload.get("family_name"))
                .role(Role.USER)
                .profileImageUrl((String) payload.get("picture"))
                .build();

        try {
            userIntegrationService.createUser(user);
        } catch (Exception ex) {
            authRepository.deleteById(authUser.getUserId());
            throw new AuthException("User creation failed. Rolled back auth record.");
        }

        return jwtUtil.generateJwtToken(authUser.getUserId(), authUser.getEmail(), user.getRole());
    }
}