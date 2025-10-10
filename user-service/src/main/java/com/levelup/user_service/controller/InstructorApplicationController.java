package com.levelup.user_service.controller;

import com.levelup.user_service.dto.InstructorApplicationDTO;
import com.levelup.user_service.service.InstructorApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/instructor-applications")
@RequiredArgsConstructor
public class InstructorApplicationController {

    private final InstructorApplicationService applicationService;

    // Student submits an application
    @PostMapping
    public ResponseEntity<InstructorApplicationDTO> submitApplication(
            @RequestHeader(value = "X-User-ID", required = false) UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdAlt,
            @RequestBody InstructorApplicationDTO dto) {
        UUID uid = userId != null ? userId : userIdAlt;
        return ResponseEntity.ok(applicationService.submit(uid, dto));
    }

    // Student checks latest application status
    @GetMapping("/me/latest")
    public ResponseEntity<InstructorApplicationDTO> myLatest(
            @RequestHeader(value = "X-User-ID", required = false) UUID userId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userIdAlt) {
        UUID uid = userId != null ? userId : userIdAlt;
        return ResponseEntity.ok(applicationService.getLatestForUser(uid));
    }

    // Admin lists pending applications
    @GetMapping("/pending")
    public ResponseEntity<List<InstructorApplicationDTO>> listPending(
            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(applicationService.listPending());
    }

    // Admin approves
    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<InstructorApplicationDTO> approve(@PathVariable UUID applicationId,
                                                            @RequestHeader(value = "X-User-ID", required = false) UUID adminUserId,
                                                            @RequestHeader(value = "X-User-Id", required = false) UUID adminUserIdAlt,
                                                            @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        UUID auid = adminUserId != null ? adminUserId : adminUserIdAlt;
        return ResponseEntity.ok(applicationService.approve(applicationId, auid));
    }

    // Admin rejects
    @PostMapping("/{applicationId}/reject")
    public ResponseEntity<InstructorApplicationDTO> reject(@PathVariable UUID applicationId,
                                                           @RequestHeader(value = "X-User-ID", required = false) UUID adminUserId,
                                                           @RequestHeader(value = "X-User-Id", required = false) UUID adminUserIdAlt,
                                                           @RequestHeader(value = "X-User-Role", required = false) String role) {
        if (role == null || !role.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        UUID auid = adminUserId != null ? adminUserId : adminUserIdAlt;
        return ResponseEntity.ok(applicationService.reject(applicationId, auid));
    }
}
