package com.levelup.user_service.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.levelup.user_service.dto.GoogleAuthRequest;
import com.levelup.user_service.dto.LoginRequest;
import com.levelup.user_service.dto.RegisterRequest;
import com.levelup.user_service.exception.InvalidCredentialsException;
import com.levelup.user_service.exception.UserAlreadyExistsException;
import com.levelup.user_service.exception.UserNotFoundException;
import com.levelup.user_service.model.AuthProvider;
import com.levelup.user_service.model.Role;
import com.levelup.user_service.model.User;
import com.levelup.user_service.repository.UserRepository;
import com.levelup.user_service.security.GoogleTokenUtil;
import com.levelup.user_service.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private GoogleTokenUtil googleTokenUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_returnsJwt_whenCredentialsValid() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("secret");

        User user = User.builder()
                .id("u1")
                .email("john@example.com")
                .password("$2a$hash")
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "$2a$hash")).thenReturn(true);
        when(jwtUtil.generateJwtToken("u1", "john@example.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.login(req);

        assertEquals("jwt-token", token);
    }

    @Test
    void login_throws_whenUserNotFound() {
        LoginRequest req = new LoginRequest();
        req.setEmail("missing@example.com");
        req.setPassword("secret");

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(req));
    }

    @Test
    void login_throws_whenPasswordInvalid() {
        LoginRequest req = new LoginRequest();
        req.setEmail("john@example.com");
        req.setPassword("bad");

        User user = User.builder()
                .id("u1")
                .email("john@example.com")
                .password("$2a$hash")
                .role(Role.USER)
                .authProvider(AuthProvider.LOCAL)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "$2a$hash")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(req));
    }

    @Test
    void register_returnsJwt_whenNewEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("new@example.com");
        req.setPassword("secret123");
        req.setFirstName("New");
        req.setLastName("User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("$2a$encoded");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("u-123");
            return u;
        });
        when(jwtUtil.generateJwtToken("u-123", "new@example.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.register(req);

        assertEquals("jwt-token", token);
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("new@example.com") &&
                        u.getPassword().equals("$2a$encoded") &&
                        u.getAuthProvider() == AuthProvider.LOCAL &&
                        u.getRole() == Role.USER
        ));
    }

    @Test
    void register_throws_whenEmailExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("exists@example.com");
        req.setPassword("secret123");
        req.setFirstName("A");
        req.setLastName("B");

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(req));
        verify(userRepository, never()).save(any());
    }

    @Test
    void googleLogin_returnsJwt_whenUserExists() {
        GoogleAuthRequest req = new GoogleAuthRequest();
        req.setToken("g-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("john@example.com");
        when(googleTokenUtil.getTokenPayload("g-token")).thenReturn(payload);

        User user = User.builder()
                .id("u1")
                .email("john@example.com")
                .role(Role.USER)
                .authProvider(AuthProvider.GOOGLE)
                .build();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateJwtToken("u1", "john@example.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.googleLogin(req);

        assertEquals("jwt-token", token);
    }

    @Test
    void googleLogin_throws_whenUserNotFound() {
        GoogleAuthRequest req = new GoogleAuthRequest();
        req.setToken("g-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("missing@example.com");
        when(googleTokenUtil.getTokenPayload("g-token")).thenReturn(payload);

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> authService.googleLogin(req));
    }

    @Test
    void googleRegister_returnsJwt_whenNewGoogleUser() {
        GoogleAuthRequest req = new GoogleAuthRequest();
        req.setToken("g-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("newgoogle@example.com");
        when(payload.getSubject()).thenReturn("google-subject");
        when(payload.get("given_name")).thenReturn("Jane");
        when(payload.get("family_name")).thenReturn("Doe");
        when(payload.get("picture")).thenReturn("http://img");
        when(googleTokenUtil.getTokenPayload("g-token")).thenReturn(payload);

        when(userRepository.existsByEmail("newgoogle@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId("u-456");
            return u;
        });
        when(jwtUtil.generateJwtToken("u-456", "newgoogle@example.com", Role.USER)).thenReturn("jwt-token");

        String token = authService.googleRegister(req);

        assertEquals("jwt-token", token);
        verify(userRepository).save(argThat(u ->
                u.getEmail().equals("newgoogle@example.com") &&
                        u.getAuthProvider() == AuthProvider.GOOGLE &&
                        Boolean.TRUE.equals(u.getEmailVerified()) &&
                        "Jane".equals(u.getFirstName()) &&
                        "Doe".equals(u.getLastName()) &&
                        "http://img".equals(u.getProfileImageUrl())
        ));
    }

    @Test
    void googleRegister_throws_whenEmailExists() {
        GoogleAuthRequest req = new GoogleAuthRequest();
        req.setToken("g-token");

        GoogleIdToken.Payload payload = mock(GoogleIdToken.Payload.class);
        when(payload.getEmail()).thenReturn("exists@example.com");
        when(googleTokenUtil.getTokenPayload("g-token")).thenReturn(payload);

        when(userRepository.existsByEmail("exists@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.googleRegister(req));
        verify(userRepository, never()).save(any());
    }
}