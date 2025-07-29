package com.levelup.course_service.service;

import com.levelup.course_service.client.UserServiceClient;
import com.levelup.course_service.exception.InstructorValidationException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstructorValidationService {
    
    private final UserServiceClient userServiceClient;
    
    public void validateInstructor(String instructorId) {
        try {
            log.info("Validating instructor: {}", instructorId);
            
            Boolean isValid = userServiceClient.validateInstructor(instructorId);
            
            if (isValid == null || !isValid) {
                throw new InstructorValidationException("Invalid instructor ID: " + instructorId);
            }
            
            log.info("Instructor validation successful: {}", instructorId);
            
        } catch (FeignException.NotFound e) {
            log.error("Instructor not found: {}", instructorId);
            throw new InstructorValidationException("Instructor not found with ID: " + instructorId);
        } catch (FeignException e) {
            log.error("Error validating instructor {}: {}", instructorId, e.getMessage());
            throw new InstructorValidationException("Failed to validate instructor: " + e.getMessage());
        }
    }
    
    public boolean isValidInstructor(String instructorId) {
        try {
            validateInstructor(instructorId);
            return true;
        } catch (InstructorValidationException e) {
            log.warn("Instructor validation failed for {}: {}", instructorId, e.getMessage());
            return false;
        }
    }
    
    public void validateInstructorOwnership(String courseInstructorId, String currentUserId) {
        if (!courseInstructorId.equals(currentUserId)) {
            throw new InstructorValidationException("You can only modify courses that you own");
        }
    }
}