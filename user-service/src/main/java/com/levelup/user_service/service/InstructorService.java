package com.levelup.user_service.service;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.dto.InstructorValidationResponseDTO;
import com.levelup.user_service.entity.Instructor;
import com.levelup.user_service.entity.Role;
import com.levelup.user_service.entity.User;
import com.levelup.user_service.repository.InstructorRepository;
import com.levelup.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;

    public String registerInstructor(UUID userId, InstructorDTO instructorDTO) {

        if (instructorRepository.existsByUserId(userId)) {
            throw new RuntimeException("Instructor already registered for this user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Instructor instructor = Instructor.builder()
                .user(user)
                .bio(instructorDTO.getBio())
                .expertise(instructorDTO.getExpertise())
                .contactDetails(new Instructor.ContactDetails(
                        instructorDTO.getContactDetails().getEmail(),
                        instructorDTO.getContactDetails().getLinkedin(),
                        instructorDTO.getContactDetails().getWebsite()))
                .build();

        user.setRole(Role.INSTRUCTOR);

        userRepository.save(user);
        instructorRepository.save(instructor);

        return "Instructor registered successfully";
    }


    public List<InstructorDTO> getAllInstructors() {
        List<Instructor> instructors = instructorRepository.findAll();
        return instructors.stream()
                .map(this::ConvertToDTO)
                .toList();
    }

    public InstructorDTO getInstructorById(UUID instructorId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        return ConvertToDTO(instructor);
    }

    public String updateInstructor(InstructorDTO instructorDTO, UUID instructorId, UUID currentUserId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (!instructor.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to update this instructor");
        }

        if (instructorDTO.getBio() != null && !instructorDTO.getBio().isBlank()) {
            instructor.setBio(instructorDTO.getBio());
        }

        if (instructorDTO.getExpertise() != null && !instructorDTO.getExpertise().isEmpty()) {
            instructor.setExpertise(instructorDTO.getExpertise());
        }

        updateContactDetails(instructor, instructorDTO.getContactDetails());

        instructorRepository.save(instructor);
        return "Instructor updated successfully";
    }

    private void updateContactDetails(Instructor instructor, InstructorDTO.ContactDetails dtoContact) {
        if (dtoContact == null) return;

        Instructor.ContactDetails contactDetails = instructor.getContactDetails();
        if (contactDetails == null) {
            contactDetails = new Instructor.ContactDetails();
        }

        if (dtoContact.getEmail() != null) {
            contactDetails.setEmail(dtoContact.getEmail());
        }
        if (dtoContact.getLinkedin() != null) {
            contactDetails.setLinkedin(dtoContact.getLinkedin());
        }
        if (dtoContact.getWebsite() != null) {
            contactDetails.setWebsite(dtoContact.getWebsite());
        }

        instructor.setContactDetails(contactDetails);
    }


    public String updateMyInstructorProfile(InstructorDTO instructorDTO, UUID currentUserId) {
        Instructor instructor = instructorRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new RuntimeException("Instructor profile not found for this user"));

        return updateInstructor(instructorDTO, instructor.getId(), currentUserId);
    }

    public String deleteInstructor(UUID instructorId, UUID currentUserId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (!instructor.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to delete this instructor");
        }

        instructorRepository.delete(instructor);
        return "Instructor deleted successfully";
    }

    private InstructorDTO ConvertToDTO(Instructor instructor) {
        User user = userRepository.findById(instructor.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found for instructor"));

        return InstructorDTO.builder()

                .profileImageUrl(user.getProfileImageUrl())
                .bio(instructor.getBio())
                .expertise(instructor.getExpertise())
                .contactDetails(InstructorDTO.ContactDetails.builder()
                        .email(instructor.getContactDetails().getEmail())
                        .linkedin(instructor.getContactDetails().getLinkedin())
                        .website(instructor.getContactDetails().getWebsite())
                        .build())
                .instructorName(user.getFirstName() + " " + user.getLastName())
                .createdAt(instructor.getCreatedAt())
                .build();
    }

    public InstructorValidationResponseDTO validateInstructorByUserId(UUID userId) {

            Instructor instructor = instructorRepository.findByUserId(userId)
                    .orElse(null);

            if (instructor == null) {
                // If instructor does not exist, return false
                return InstructorValidationResponseDTO.builder()
                        .instructorId(null)
                        .isValidInstructor(false)
                        .build();
            }

            // If instructor exists, return true with instructor ID
            return InstructorValidationResponseDTO.builder()
                    .instructorId(instructor.getId())
                    .isValidInstructor(true)
                    .build();
    }

    public InstructorDTO getMyInstructorProfile(UUID userId) {
        Instructor instructor = instructorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Instructor profile not found for this user"));

        return ConvertToDTO(instructor);
    }
}
