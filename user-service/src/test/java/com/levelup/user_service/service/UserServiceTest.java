package com.levelup.user_service.service;

import com.levelup.user_service.dto.UserDTO;
import com.levelup.user_service.entity.Role;
import com.levelup.user_service.entity.User;
import com.levelup.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

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
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000001");

        User user = User.builder()
                .id(id)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        UserDTO result = userService.getUserById(id);

        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getUserById_throwsException_whenUserNotFound() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000002");

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserById(id));
    }

    @Test
    void createUser_savesUser_andReturnsDTO() {
        UUID id = UUID.fromString("00000000-0000-0000-0000-000000000003");

        UserDTO dto = UserDTO.builder()
                .id(id)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .role(Role.USER)
                .build();

        User user = User.builder()
                .id(id)
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