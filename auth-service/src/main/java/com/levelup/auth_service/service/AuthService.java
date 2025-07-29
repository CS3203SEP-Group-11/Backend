package com.levelup.auth_service.service;

import com.levelup.auth_service.dto.*;
import com.levelup.auth_service.exception.*;
import com.levelup.auth_service.model.AuthProvider;
import com.levelup.auth_service.model.AuthUser;
import com.levelup.auth_service.repository.AuthRepository;
import com.levelup.auth_service.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final UserIntegrationService userIntegrationService;

    public AuthResponse login(LoginRequest request) {
        AuthUser authUser = authRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPassword())) {
            throw new InvalidCredentialsException("Invalid credentials");
        }

        UserDTO user = userIntegrationService.getUserById(authUser.getUserId());

        return buildAuthResponse(authUser, user);
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
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
            throw new AuthException("User creation failed. Rolled back auth record.", ex);
        }

        return buildAuthResponse(authUser, user);
    }

    @Transactional
    public AuthResponse processOAuth2Login(OAuth2AuthenticationToken authenticationToken) {
        OAuth2User oAuth2User = authenticationToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");
        String profileImageUrl = oAuth2User.getAttribute("picture");
        String googleId = oAuth2User.getAttribute("sub");

        log.info("OAuth2 login user: {}", oAuth2User);

        AuthUser authUser = authRepository.findByEmail(email).orElse(null);

        if (authUser == null) {
            authUser = AuthUser.builder()
                    .email(email)
                    .authProvider(AuthProvider.GOOGLE)
                    .googleId(googleId)
                    .emailVerified(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();

            authUser = authRepository.save(authUser);
        }

        UserDTO user = userIntegrationService.getUserById(authUser.getUserId());

        if (user == null) {
            user = UserDTO.builder()
                    .id(authUser.getUserId())
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .profileImageUrl(profileImageUrl)
                    .role(Role.USER)
                    .build();

            try {
                userIntegrationService.createUser(user);
            } catch (Exception ex) {
                authRepository.deleteById(authUser.getUserId());
                throw new AuthException("User creation failed. Rolled back auth record.", ex);
            }
        }

        return buildAuthResponse(authUser, user);
    }

    private AuthResponse buildAuthResponse(AuthUser authUser, UserDTO user) {
        String token = jwtUtils.generateJwtToken(authUser.getUserId(), authUser.getEmail(), user.getRole());

        return AuthResponse.builder()
                .userId(authUser.getUserId())
                .token(token)
                .tokenType("Bearer")
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .profilePictureUrl(user.getProfileImageUrl())
                .build();
    }
}
