package com.levelup.course_service.service;

import com.levelup.course_service.dto.CertificateDTO;

import java.util.List;
import java.util.UUID;

public interface CertificateService {
    String requestCertificate(UUID enrollmentId, UUID userId);
    List<CertificateDTO> getCertificatesByUser(UUID currentUserId);
}
