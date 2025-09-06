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
        log.info(userId);
        return ResponseEntity.ok(instructorService.registerInstructor(userId, instructorDTO));
    }

    @GetMapping
    public ResponseEntity<List<InstructorDTO>> getAllInstructors() {
        return ResponseEntity.ok(instructorService.getAllInstructors());
    }

    @GetMapping("/me")
    public ResponseEntity<InstructorDTO> getMyInstructorProfile(@RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(instructorService.getMyInstructorProfile(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<String> updateMyInstructorProfile(@RequestBody InstructorDTO instructorDTO, @RequestHeader("X-User-ID") String userId) {
        return ResponseEntity.ok(instructorService.updateMyInstructorProfile(instructorDTO, userId));
    }

    @GetMapping("/{InstructorId}")
    public ResponseEntity<InstructorDTO> getInstructorById(@PathVariable String InstructorId) {
        return ResponseEntity.ok(instructorService.getInstructorById(InstructorId));
    }

    @PutMapping("/{InstructorId}")
    public ResponseEntity<String> updateInstructor(@RequestBody InstructorDTO instructorDTO, @PathVariable String InstructorId, @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(instructorService.updateInstructor(instructorDTO, InstructorId, currentUserId));
    }

    @DeleteMapping("/{InstructorId}")
    public ResponseEntity<String> deleteInstructor(@PathVariable String InstructorId, @RequestHeader("X-User-ID") String currentUserId) {
        return ResponseEntity.ok(instructorService.deleteInstructor(InstructorId, currentUserId));
    }


    @GetMapping("/{userId}/validate")
    public ResponseEntity<InstructorValidationResponseDTO> validateInstructorByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(instructorService.validateInstructorByUserId(userId));
    }
}

