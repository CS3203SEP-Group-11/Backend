package com.levelup.user_service.service;

import com.levelup.user_service.dto.UserDTO;
import com.levelup.user_service.entity.User;
import com.levelup.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(this::convertToDTO)
                .toList();
    }

    public String updateUser(UserDTO userDTO, UUID userId, String currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to update this user");
        }

        if (userDTO.getFirstName() != null && !userDTO.getFirstName().isBlank()) {
            user.setFirstName(userDTO.getFirstName());
        }

        if (userDTO.getLastName() != null && !userDTO.getLastName().isBlank()) {
            user.setLastName(userDTO.getLastName());
        }

        if (userDTO.getProfileImageUrl() != null && !userDTO.getProfileImageUrl().isBlank()) {
            user.setProfileImageUrl(userDTO.getProfileImageUrl());
        }

        if (userDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(userDTO.getDateOfBirth());
        }

        if (userDTO.getLanguagePreference() != null && !userDTO.getLanguagePreference().isBlank()) {
            user.setLanguagePreference(userDTO.getLanguagePreference());
        }

        userRepository.save(user);
        return "User updated successfully";
    }

    public String deleteUser(UUID userId, String currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to delete this user");
        }

        userRepository.delete(user);
        return "User deleted successfully";
    }

    public String updateStripeCustomerId(UUID userId, String stripeCustomerId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setStripeCustomerId(stripeCustomerId);
        userRepository.save(user);

        return "Stripe customer ID updated successfully";
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .role(user.getRole())
                .dateOfBirth(user.getDateOfBirth())
                .languagePreference(user.getLanguagePreference())
                .stripeCustomerId(user.getStripeCustomerId())
                .isSubscribed(user.getIsSubscribed())
                .build();
    }

    public UserDTO createUser(UserDTO userDTO) {
        User user = User.builder()
                .id(userDTO.getId())
                .firstName(userDTO.getFirstName())
                .lastName(userDTO.getLastName())
                .email(userDTO.getEmail())
                .profileImageUrl(userDTO.getProfileImageUrl())
                .role(userDTO.getRole())
                .dateOfBirth(userDTO.getDateOfBirth())
                .languagePreference(userDTO.getLanguagePreference())
                .build();

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public java.util.Map<String, Object> getUserAnalytics() {
        java.util.Map<String, Object> analytics = new java.util.HashMap<>();
        
        List<User> allUsers = userRepository.findAll();
        
        // Basic user statistics
        int totalUsers = allUsers.size();
        long activeUsers = allUsers.stream()
                .mapToLong(user -> user.getIsSubscribed() != null && user.getIsSubscribed() ? 1 : 0)
                .sum();
        
        // Calculate users created this month (last 30 days)
        java.time.Instant thirtyDaysAgo = java.time.Instant.now().minus(30, java.time.temporal.ChronoUnit.DAYS);
        long newUsersThisMonth = allUsers.stream()
                .mapToLong(user -> user.getCreatedAt() != null && user.getCreatedAt().isAfter(thirtyDaysAgo) ? 1 : 0)
                .sum();
        
        analytics.put("totalUsers", totalUsers);
        analytics.put("activeUsers", activeUsers);
        analytics.put("newUsersThisMonth", newUsersThisMonth);
        
        // Generate last 8 days user growth data
        java.util.List<java.util.Map<String, Object>> userGrowth = new java.util.ArrayList<>();
        java.time.LocalDate today = java.time.LocalDate.now();
        
        for (int i = 7; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            java.time.Instant startOfDay = date.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            java.time.Instant endOfDay = date.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
            
            long usersCreatedOnDate = allUsers.stream()
                    .mapToLong(user -> user.getCreatedAt() != null && 
                        user.getCreatedAt().isAfter(startOfDay) && 
                        user.getCreatedAt().isBefore(endOfDay) ? 1 : 0)
                    .sum();
            
            java.util.Map<String, Object> dayData = new java.util.HashMap<>();
            dayData.put("label", date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd")));
            dayData.put("value", usersCreatedOnDate);
            userGrowth.add(dayData);
        }
        
        analytics.put("userGrowth", userGrowth);
        
        return analytics;
    }
}
