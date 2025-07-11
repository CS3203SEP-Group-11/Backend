package com.levelup.user_service.controller;

import com.levelup.user_service.dto.InstructorDTO;
import com.levelup.user_service.service.InstructorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}
