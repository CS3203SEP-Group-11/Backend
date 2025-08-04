package com.levelup.user_service.service;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.model.Instructor;
import com.levelup.user_service.model.Role;
import com.levelup.user_service.model.User;
import com.levelup.user_service.repository.InstructorRepository;
import com.levelup.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InstructorService {

    private final InstructorRepository instructorRepository;
    private final UserRepository userRepository;

    public String registerInstructor(String userId, InstructorDTO instructorDTO) {

        if (instructorRepository.existsByUserId(userId)) {
            throw new RuntimeException("Instructor already registered for this user");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Instructor instructor = Instructor.builder()
                .userId(userId)
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

    public InstructorDTO getInstructorByUserId(String instructorId) {
        Instructor instructor = instructorRepository.findByUserId(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        return ConvertToDTO(instructor);
    }

    public String updateInstructor(InstructorDTO instructorDTO, String instructorId, String currentUserId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (!instructor.getUserId().equals(currentUserId)) {
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


    public String deleteInstructor(String instructorId, String currentUserId) {
        Instructor instructor = instructorRepository.findById(instructorId)
                .orElseThrow(() -> new RuntimeException("Instructor not found"));

        if (!instructor.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Unauthorized to delete this instructor");
        }

        instructorRepository.delete(instructor);
        return "Instructor deleted successfully";
    }

    private InstructorDTO ConvertToDTO(Instructor instructor) {
        User user = userRepository.findById(instructor.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found for instructor"));

        return InstructorDTO.builder()
                .id(instructor.getId())
                .userId(instructor.getUserId())
                .bio(instructor.getBio())
                .expertise(instructor.getExpertise())
                .contactDetails(InstructorDTO.ContactDetails.builder()
                        .email(instructor.getContactDetails().getEmail())
                        .linkedin(instructor.getContactDetails().getLinkedin())
                        .website(instructor.getContactDetails().getWebsite())
                        .build())
                .instructorName(user.getFirstName() + " " + user.getLastName())
                .build();
    }
}
