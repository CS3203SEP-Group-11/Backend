package com.levelup.user_service.service;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.dto.InstructorValidationResponseDTO;
import com.levelup.user_service.entity.Instructor;
import com.levelup.user_service.entity.Role;
import com.levelup.user_service.entity.User;
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
import java.util.UUID;

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

    private static final UUID U1 = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID U2 = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID I1 = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID I2 = UUID.fromString("00000000-0000-0000-0000-000000000012");

    @Test
    void registerInstructor_registersSuccessfully_whenUserExistsAndNotAlreadyInstructor() {
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
                .id(U1)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.USER)
                .build();

        when(instructorRepository.existsByUserId(U1)).thenReturn(false);
        when(userRepository.findById(U1)).thenReturn(Optional.of(user));

        String result = instructorService.registerInstructor(U1, dto);

        assertEquals("Instructor registered successfully", result);
        verify(userRepository).save(argThat(savedUser -> savedUser.getRole() == Role.INSTRUCTOR));
        verify(instructorRepository).save(any(Instructor.class));
    }

    @Test
    void registerInstructor_throwsException_whenInstructorAlreadyExists() {
        InstructorDTO dto = InstructorDTO.builder().build();

        when(instructorRepository.existsByUserId(U1)).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.registerInstructor(U1, dto));

        assertEquals("Instructor already registered for this user", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(instructorRepository, never()).save(any());
    }

    @Test
    void registerInstructor_throwsException_whenUserNotFound() {
        InstructorDTO dto = InstructorDTO.builder().build();

        when(instructorRepository.existsByUserId(U1)).thenReturn(false);
        when(userRepository.findById(U1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.registerInstructor(U1, dto));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void getAllInstructors_returnsListOfInstructorDTOs() {
        Instructor instructor1 = Instructor.builder()
                .id(I1)
                .user(User.builder().id(U1).build())
                .bio("Bio 1")
                .expertise(Arrays.asList("Java"))
                .contactDetails(new Instructor.ContactDetails("test1@example.com", "linkedin1", "website1"))
                .build();

        Instructor instructor2 = Instructor.builder()
                .id(I2)
                .user(User.builder().id(U2).build())
                .bio("Bio 2")
                .expertise(Arrays.asList("Python"))
                .contactDetails(new Instructor.ContactDetails("test2@example.com", "linkedin2", "website2"))
                .build();

        User user1 = User.builder()
                .id(U1)
                .firstName("John")
                .lastName("Doe")
                .profileImageUrl("image1.jpg")
                .build();

        User user2 = User.builder()
                .id(U2)
                .firstName("Jane")
                .lastName("Smith")
                .profileImageUrl("image2.jpg")
                .build();

        when(instructorRepository.findAll()).thenReturn(Arrays.asList(instructor1, instructor2));
        when(userRepository.findById(U1)).thenReturn(Optional.of(user1));
        when(userRepository.findById(U2)).thenReturn(Optional.of(user2));

        List<InstructorDTO> result = instructorService.getAllInstructors();

        assertEquals(2, result.size());
        // Assert fields present in the updated DTO
        assertEquals("Bio 1", result.get(0).getBio());
        assertEquals("Bio 2", result.get(1).getBio());
        assertEquals("test1@example.com", result.get(0).getContactDetails().getEmail());
        assertEquals("test2@example.com", result.get(1).getContactDetails().getEmail());
    }

    @Test
    void getInstructorById_returnsInstructorDTO_whenInstructorExists() {
        Instructor instructor = Instructor.builder()
                .id(I1)
                .user(User.builder().id(U1).build())
                .bio("Experienced instructor")
                .expertise(Arrays.asList("Java", "Spring"))
                .contactDetails(new Instructor.ContactDetails("test@example.com", "linkedin", "website"))
                .build();

        User user = User.builder()
                .id(U1)
                .firstName("John")
                .lastName("Doe")
                .profileImageUrl("image.jpg")
                .build();

        when(instructorRepository.findById(I1)).thenReturn(Optional.of(instructor));
        when(userRepository.findById(U1)).thenReturn(Optional.of(user));

        InstructorDTO result = instructorService.getInstructorById(I1);

        assertEquals("Experienced instructor", result.getBio());
        assertEquals("test@example.com", result.getContactDetails().getEmail());
    }

    @Test
    void getInstructorById_throwsException_whenInstructorNotFound() {
        when(instructorRepository.findById(I1)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.getInstructorById(I1));

        assertEquals("Instructor not found", exception.getMessage());
    }

    @Test
    void updateInstructor_updatesSuccessfully_whenAuthorized() {
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
                .id(I1)
                .user(User.builder().id(U1).build())
                .bio("Old bio")
                .contactDetails(new Instructor.ContactDetails("old@example.com", "old-linkedin", "old-website"))
                .build();

        when(instructorRepository.findById(I1)).thenReturn(Optional.of(instructor));

        String result = instructorService.updateInstructor(dto, I1, U1);

        assertEquals("Instructor updated successfully", result);
        verify(instructorRepository).save(argThat(savedInstructor ->
                "Updated bio".equals(savedInstructor.getBio()) &&
                        savedInstructor.getExpertise().contains("Docker") &&
                        "updated@example.com".equals(savedInstructor.getContactDetails().getEmail())
        ));
    }

    @Test
    void updateInstructor_throwsException_whenUnauthorized() {
        InstructorDTO dto = InstructorDTO.builder().build();
        Instructor instructor = Instructor.builder()
                .id(I1)
                .user(User.builder().id(U1).build())
                .build();

        when(instructorRepository.findById(I1)).thenReturn(Optional.of(instructor));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.updateInstructor(dto, I1, U2));

        assertEquals("Unauthorized to update this instructor", exception.getMessage());
        verify(instructorRepository, never()).save(any());
    }

    @Test
    void deleteInstructor_deletesSuccessfully_whenAuthorized() {
        Instructor instructor = Instructor.builder()
                .id(I1)
                .user(User.builder().id(U1).build())
                .build();

        when(instructorRepository.findById(I1)).thenReturn(Optional.of(instructor));

        String result = instructorService.deleteInstructor(I1, U1);

        assertEquals("Instructor deleted successfully", result);
        verify(instructorRepository).delete(instructor);
    }

    @Test
    void deleteInstructor_throwsException_whenUnauthorized() {
        Instructor instructor = Instructor.builder()
                .id(I1)
                .user(User.builder().id(U1).build())
                .build();

        when(instructorRepository.findById(I1)).thenReturn(Optional.of(instructor));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> instructorService.deleteInstructor(I1, U2));

        assertEquals("Unauthorized to delete this instructor", exception.getMessage());
        verify(instructorRepository, never()).delete(any());
    }

    @Test
    void validateInstructorByUserId_returnsTrue_whenInstructorExists() {
        Instructor instructor = Instructor.builder()
                .id(I1)
                .user(User.builder().id(U1).build())
                .build();

        when(instructorRepository.findByUserId(U1)).thenReturn(Optional.of(instructor));

        InstructorValidationResponseDTO result = instructorService.validateInstructorByUserId(U1);

        assertTrue(result.getIsValidInstructor());
        assertEquals(I1, result.getInstructorId());
    }

    @Test
    void validateInstructorByUserId_returnsFalse_whenInstructorNotExists() {
        when(instructorRepository.findByUserId(U1)).thenReturn(Optional.empty());

        InstructorValidationResponseDTO result = instructorService.validateInstructorByUserId(U1);

        assertFalse(result.getIsValidInstructor());
        assertNull(result.getInstructorId());
    }
}