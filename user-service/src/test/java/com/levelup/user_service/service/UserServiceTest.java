package com.levelup.user_service.service;

import com.levelup.user_service.dto.UserDTO;
import com.levelup.user_service.model.User;
import com.levelup.user_service.model.Role;
import com.levelup.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_returnsUserDTO_whenUserExists() {
        User user = User.builder()
                .id("1")
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findById("1")).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById("1");

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getUserById_throwsException_whenUserNotFound() {
        when(userRepository.findById("2")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById("2"));
    }

    @Test
    void createUser_savesUser_andReturnsDTO() {
        UserDTO dto = UserDTO.builder()
                .id("3")
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .role(Role.USER)
                .build();

        User user = User.builder()
                .id("3")
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDTO result = userService.createUser(dto);

        assertEquals("Alice", result.getFirstName());
        assertEquals("Smith", result.getLastName());
        assertEquals("alice@example.com", result.getEmail());
    }
}