package com.levelup.course_service.dto;

import com.levelup.course_service.entity.CourseEnrollment;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateDTO {
    private String publicId;
    private String certificateUrl;
    private String courseTitle;
}
