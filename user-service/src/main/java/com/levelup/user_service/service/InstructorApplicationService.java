package com.levelup.user_service.service;

import com.levelup.user_service.dto.InstructorApplicationDTO;
import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.entity.*;
import com.levelup.user_service.repository.InstructorApplicationRepository;
import com.levelup.user_service.repository.InstructorRepository;
import com.levelup.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InstructorApplicationService {

    private final InstructorApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final InstructorRepository instructorRepository;

    public InstructorApplicationDTO submit(UUID userId, InstructorApplicationDTO dto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        InstructorApplication app = InstructorApplication.builder()
                .user(user)
                .bio(dto.getBio())
                .expertise(dto.getExpertise())
                .experienceYears(dto.getExperienceYears())
                .status(ApplicationStatus.PENDING)
                .build();
        app = applicationRepository.save(app);
        return toDTO(app);
    }

    public List<InstructorApplicationDTO> listPending() {
        return applicationRepository.findByStatus(ApplicationStatus.PENDING)
                .stream().map(this::toDTO).toList();
    }

    public InstructorApplicationDTO getLatestForUser(UUID userId) {
        return applicationRepository.findTopByUser_IdOrderByCreatedAtDesc(userId)
                .map(this::toDTO)
                .orElse(null);
    }

    public InstructorApplicationDTO approve(UUID applicationId, UUID adminUserId) {
        InstructorApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        app.setStatus(ApplicationStatus.APPROVED);
        applicationRepository.save(app);

        // Promote user to INSTRUCTOR and create Instructor profile shell
        User user = app.getUser();
        user.setRole(Role.INSTRUCTOR);
        userRepository.save(user);

        if (!instructorRepository.existsByUserId(user.getId())) {
            Instructor instructor = Instructor.builder()
                    .user(user)
                    .bio(app.getBio())
                    .expertise(app.getExpertise() != null ? List.of(app.getExpertise().split(",")) : null)
                    .contactDetails(new Instructor.ContactDetails(
                            user.getEmail(),
                            null,
                            null))
                    .build();
            instructorRepository.save(instructor);
        }

        return toDTO(app);
    }

    public InstructorApplicationDTO reject(UUID applicationId, UUID adminUserId) {
        InstructorApplication app = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));
        app.setStatus(ApplicationStatus.REJECTED);
        applicationRepository.save(app);
        return toDTO(app);
    }

    private InstructorApplicationDTO toDTO(InstructorApplication app) {
        return InstructorApplicationDTO.builder()
                .id(app.getId())
                .userId(app.getUser().getId())
                .applicantName(app.getUser().getFirstName() + " " + app.getUser().getLastName())
                .experienceYears(app.getExperienceYears())
                .bio(app.getBio())
                .expertise(app.getExpertise())
                .status(app.getStatus())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
