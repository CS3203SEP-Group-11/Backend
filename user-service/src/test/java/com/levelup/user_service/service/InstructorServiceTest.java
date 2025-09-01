package com.levelup.user_service.service;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.dto.InstructorValidationResponseDTO;
import com.levelup.user_service.model.Instructor;
import com.levelup.user_service.model.Role;
import com.levelup.user_service.model.User;
import com.levelup.user_service.repository.InstructorRepository;
import com.levelup.user_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstructorServiceTest {

    @Mock
    private InstructorRepository instructorRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InstructorService instructorService;

    @Test
    void registerInstructor_registersSuccessfully_whenUserExistsAndNotAlreadyInstructor() {
        String userId = "user1";
        InstructorDTO dto = InstructorDTO.builder()
                .bio("Experienced instructor")
                .expertise(Arrays.asList("Java", "Spring"))
                .contactDetails(InstructorDTO.ContactDetails.builder()
                        .email("instructor@example.com")
                        .linkedin("linkedin.com/instructor")
                        .website("instructor.com")
                        .build())
                .build();

        User user = User.builder()
                .id(userId)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(instructorRepository.existsByUserId(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        String result = instructorService.registerInstructor(userId, dto);

        assertEquals("Instructor registered successfully", result);
        verify(userRepository).save(argThat(savedUser -> savedUser.getRole() == Role.INSTRUCTOR));
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    void registerInstructor_throwsException_whenInstructorAlreadyExists() {
        String userId = "user1";
        InstructorDTO dto = InstructorDTO.builder().build();

        when(instructorRepository.existsByUserId(userId)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.registerInstructor(userId, dto));

        assertEquals("Instructor already registered for this user", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(instructorRepository, never()).save(any());
    }

    @Test
    void registerInstructor_throwsException_whenUserNotFound() {
        String userId = "user1";
        InstructorDTO dto = InstructorDTO.builder().build();

        when(instructorRepository.existsByUserId(userId)).thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.registerInstructor(userId, dto));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getAllInstructors_returnsListOfInstructorDTOs() {
        Instructor instructor1 = Instructor.builder()
                .id("1")
                .userId("user1")
                .bio("Bio 1")
                .expertise(Arrays.asList("Java"))
                .contactDetails(new Instructor.ContactDetails("test1@example.com", "linkedin1", "website1"))
                .build();

        Instructor instructor2 = Instructor.builder()
                .id("2")
                .userId("user2")
                .bio("Bio 2")
                .expertise(Arrays.asList("Python"))
                .contactDetails(new Instructor.ContactDetails("test2@example.com", "linkedin2", "website2"))
                .build();

        User user1 = User.builder()
                .id("user1")
                .firstName("John")
                .lastName("Doe")
                .profileImageUrl("image1.jpg")
                .build();

        User user2 = User.builder()
                .id("user2")
                .firstName("Jane")
                .lastName("Smith")
                .profileImageUrl("image2.jpg")
                .build();

        when(instructorRepository.findAll()).thenReturn(Arrays.asList(instructor1, instructor2));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user1));
        when(userRepository.findById("user2")).thenReturn(Optional.of(user2));

        List<InstructorDTO> result = instructorService.getAllInstructors();

        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getInstructorName());
        assertEquals("Jane Smith", result.get(1).getInstructorName());
    }

    @Test
    void getInstructorById_returnsInstructorDTO_whenInstructorExists() {
        String instructorId = "instructor1";
        Instructor instructor = Instructor.builder()
                .id(instructorId)
                .userId("user1")
                .bio("Experienced instructor")
                .expertise(Arrays.asList("Java", "Spring"))
                .contactDetails(new Instructor.ContactDetails("test@example.com", "linkedin", "website"))
                .build();

        User user = User.builder()
                .id("user1")
                .firstName("John")
                .lastName("Doe")
                .profileImageUrl("image.jpg")
                .build();

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));
        when(userRepository.findById("user1")).thenReturn(Optional.of(user));

        InstructorDTO result = instructorService.getInstructorById(instructorId);

        assertEquals("Experienced instructor", result.getBio());
        assertEquals("John Doe", result.getInstructorName());
        assertEquals("test@example.com", result.getContactDetails().getEmail());
    }

    @Test
    void getInstructorById_throwsException_whenInstructorNotFound() {
        String instructorId = "nonexistent";

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.getInstructorById(instructorId));

        assertEquals("Instructor not found", exception.getMessage());
    }

    @Test
    void updateInstructor_updatesSuccessfully_whenAuthorized() {
        String instructorId = "instructor1";
        String currentUserId = "user1";

        InstructorDTO dto = InstructorDTO.builder()
                .bio("Updated bio")
                .expertise(Arrays.asList("Java", "Spring", "Docker"))
                .contactDetails(InstructorDTO.ContactDetails.builder()
                        .email("updated@example.com")
                        .linkedin("updated-linkedin")
                        .website("updated-website")
                        .build())
                .build();

        Instructor instructor = Instructor.builder()
                .id(instructorId)
                .userId(currentUserId)
                .bio("Old bio")
                .contactDetails(new Instructor.ContactDetails("old@example.com", "old-linkedin", "old-website"))
                .build();

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        String result = instructorService.updateInstructor(dto, instructorId, currentUserId);

        assertEquals("Instructor updated successfully", result);
        verify(instructorRepository).save(argThat(savedInstructor ->
                "Updated bio".equals(savedInstructor.getBio()) &&
                        savedInstructor.getExpertise().contains("Docker") &&
                        "updated@example.com".equals(savedInstructor.getContactDetails().getEmail())
        ));
    }

    @Test
    void updateInstructor_throwsException_whenUnauthorized() {
        String instructorId = "instructor1";
        String currentUserId = "user2";
        String instructorUserId = "user1";

        InstructorDTO dto = InstructorDTO.builder().build();
        Instructor instructor = Instructor.builder()
                .id(instructorId)
                .userId(instructorUserId)
                .build();

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.updateInstructor(dto, instructorId, currentUserId));

        assertEquals("Unauthorized to update this instructor", exception.getMessage());
        verify(instructorRepository, never()).save(any());
    }

    @Test
    void deleteInstructor_deletesSuccessfully_whenAuthorized() {
        String instructorId = "instructor1";
        String currentUserId = "user1";

        Instructor instructor = Instructor.builder()
                .id(instructorId)
                .userId(currentUserId)
                .build();

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        String result = instructorService.deleteInstructor(instructorId, currentUserId);

        assertEquals("Instructor deleted successfully", result);
        verify(instructorRepository).delete(instructor);
    }

    @Test
    void deleteInstructor_throwsException_whenUnauthorized() {
        String instructorId = "instructor1";
        String currentUserId = "user2";
        String instructorUserId = "user1";

        Instructor instructor = Instructor.builder()
                .id(instructorId)
                .userId(instructorUserId)
                .build();

        when(instructorRepository.findById(instructorId)).thenReturn(Optional.of(instructor));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.deleteInstructor(instructorId, currentUserId));

        assertEquals("Unauthorized to delete this instructor", exception.getMessage());
        verify(instructorRepository, never()).delete(any());
    }

    @Test
    void validateInstructorByUserId_returnsTrue_whenInstructorExists() {
        String userId = "user1";
        Instructor instructor = Instructor.builder()
                .id("instructor1")
                .userId(userId)
                .build();

        when(instructorRepository.findByUserId(userId)).thenReturn(Optional.of(instructor));

        InstructorValidationResponseDTO result = instructorService.validateInstructorByUserId(userId);

        assertTrue(result.getIsValidInstructor());
        assertEquals("instructor1", result.getInstructorId());
    }

    @Test
    void validateInstructorByUserId_returnsFalse_whenInstructorNotExists() {
        String userId = "user1";

        when(instructorRepository.findByUserId(userId)).thenReturn(Optional.empty());

        InstructorValidationResponseDTO result = instructorService.validateInstructorByUserId(userId);

        assertFalse(result.getIsValidInstructor());
        assertNull(result.getInstructorId());
    }
}