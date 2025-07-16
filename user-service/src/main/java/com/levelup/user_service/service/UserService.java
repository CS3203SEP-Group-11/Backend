package com.levelup.user_service.service;

import com.levelup.user_service.dto.UserDTO;
import com.levelup.user_service.model.User;
import com.levelup.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserDTO getUserById(String userId) {
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

    public String updateUser(UserDTO userDTO, String userId, String currentUserId) {
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

    public String deleteUser(String userId, String currentUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to delete this user");
        }

        userRepository.delete(user);
        return "User deleted successfully";
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
}
