package com.levelup.user_service.controller;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.dto.InstructorValidationResponseDTO;
import com.levelup.user_service.model.Instructor;
import com.levelup.user_service.service.InstructorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Slf4j
public class InstructorController {

    private final InstructorService instructorService;

    @PostMapping
    public ResponseEntity<String> registerInstructor(@RequestHeader ("X-User-ID") String userId, @RequestBody InstructorDTO instructorDTO) {
        return ResponseEntity.ok(instructorService.registerInstructor(userId, instructorDTO));
    }

    @GetMapping
    public ResponseEntity<List<InstructorDTO>> getAllInstructors() {
        return ResponseEntity.ok(instructorService.getAllInstructors());
    }

    @GetMapping("/{InstructorId}")
    public ResponseEntity<InstructorDTO> getInstructorByUserId(@PathVariable String InstructorId) {
        return ResponseEntity.ok(instructorService.getInstructorByUserId(InstructorId));
    }

    @PutMapping("/{InstructorId}")
    public ResponseEntity<String> updateInstructor(@RequestBody InstructorDTO instructorDTO, @PathVariable String InstructorId, @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(instructorService.updateInstructor(instructorDTO, InstructorId, currentUserId));
    }

    @DeleteMapping("/{InstructorId}")
    public ResponseEntity<String> deleteInstructor(@PathVariable String InstructorId, @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(instructorService.deleteInstructor(InstructorId, currentUserId));
    }


@GetMapping("/{instructorId}/validate")
    public ResponseEntity<InstructorValidationResponseDTO> validateInstructor(@PathVariable String instructorId) {
        try {
            // Check if instructor exists
            boolean exists = instructorService.getInstructorByUserId(instructorId) != null;
            log.info("Instructor ID {} exists: {}", instructorId, exists);
            
            // Return DTO with validation result
            InstructorValidationResponseDTO response = InstructorValidationResponseDTO.builder()
                    .instructorId(instructorId)
                    .isValidInstructor(exists)
                    .build();
                    
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking instructor ID {}: {}", instructorId, e.getMessage());
            
            // Return DTO with false validation
            InstructorValidationResponseDTO response = InstructorValidationResponseDTO.builder()
                    .instructorId(instructorId)
                    .isValidInstructor(false)
                    .build();
                    
            return ResponseEntity.ok(response);
        }
    }
}

