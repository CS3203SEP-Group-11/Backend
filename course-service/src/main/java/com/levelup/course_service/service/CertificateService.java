package com.levelup.course_service.service;

import java.util.UUID;

public interface CertificateService {
    String requestCertificate(UUID enrollmentId, UUID userId);
}
