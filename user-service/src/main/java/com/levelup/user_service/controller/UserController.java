package com.levelup.user_service.controller;

import com.levelup.user_service.dto.UserDTO;
import com.levelup.user_service.security.JwtUtil;
import com.levelup.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.createUser(userDTO));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID userId) {
        try {
            UserDTO user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            log.error("User not found with ID: {}", userId);
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{userId}")
    public ResponseEntity<String> updateUser(@RequestBody UserDTO userDTO, @PathVariable UUID userId,
            @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(userService.updateUser(userDTO, userId, currentUserId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable UUID userId,
            @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(userService.deleteUser(userId, currentUserId));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(HttpServletRequest request) {
        // Extract JWT token from cookie
        String jwtToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName())) {
                    jwtToken = cookie.getValue();
                    break;
                }
            }
        }

        if (jwtToken == null || !jwtUtil.validateJwtToken(jwtToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Extract user ID from JWT token
        String userIdStr = jwtUtil.getUserIdFromJwtToken(jwtToken);
        UUID userId = UUID.fromString(userIdStr);
        
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{userId}/stripe-customer")
    public ResponseEntity<String> updateStripeCustomerId(@PathVariable UUID userId,
            @RequestBody String stripeCustomerId) {
        return ResponseEntity.ok(userService.updateStripeCustomerId(userId, stripeCustomerId));
    }

    @GetMapping("/analytics")
    public ResponseEntity<java.util.Map<String, Object>> getUserAnalytics(
            @RequestHeader(value = "X-User-Role", required = false) String userRole) {
        
        if (!"ADMIN".equals(userRole)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(userService.getUserAnalytics());
    }
}
