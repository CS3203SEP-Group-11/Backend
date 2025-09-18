package com.levelup.user_service.controller;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.dto.InstructorValidationResponseDTO;
import com.levelup.user_service.service.InstructorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instructors")
@RequiredArgsConstructor
@Slf4j
public class InstructorController {

    private final InstructorService instructorService;

    @PostMapping
    public ResponseEntity<String> registerInstructor(@RequestHeader ("X-User-ID") UUID userId, @RequestBody InstructorDTO instructorDTO) {
        return ResponseEntity.ok(instructorService.registerInstructor(userId, instructorDTO));
    }

    @GetMapping
    public ResponseEntity<List<InstructorDTO>> getAllInstructors() {
        return ResponseEntity.ok(instructorService.getAllInstructors());
    }

    @GetMapping("/me")
    public ResponseEntity<InstructorDTO> getMyInstructorProfile(@RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(instructorService.getMyInstructorProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyInstructorProfile(@RequestBody InstructorDTO instructorDTO, @RequestHeader("X-User-ID") UUID userId) {
        return ResponseEntity.ok(instructorService.updateMyInstructorProfile(instructorDTO, userId));
    }

    @GetMapping("/{InstructorId}")
    public ResponseEntity<InstructorDTO> getInstructorById(@PathVariable UUID InstructorId) {
        return ResponseEntity.ok(instructorService.getInstructorById(InstructorId));
    }

    @PutMapping("/{InstructorId}")
    public ResponseEntity<String> updateInstructor(@RequestBody InstructorDTO instructorDTO, @PathVariable UUID InstructorId, @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(instructorService.updateInstructor(instructorDTO, InstructorId, currentUserId));
    }

    @DeleteMapping("/{InstructorId}")
    public ResponseEntity<String> deleteInstructor(@PathVariable UUID InstructorId, @RequestHeader("X-User-ID") UUID currentUserId) {
        return ResponseEntity.ok(instructorService.deleteInstructor(InstructorId, currentUserId));
    }


    @GetMapping("/{userId}/validate")
    public ResponseEntity<InstructorValidationResponseDTO> validateInstructorByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(instructorService.validateInstructorByUserId(userId));
    }
}

