package com.levelup.course_service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.levelup.course_service.client.UserServiceClient;
import com.levelup.course_service.dto.CertificateDTO;
import com.levelup.course_service.dto.CourseDTO;
import com.levelup.course_service.dto.UserDTO;
import com.levelup.course_service.entity.Certificate;
import com.levelup.course_service.entity.Course;
import com.levelup.course_service.entity.CourseEnrollment;
import com.levelup.course_service.repository.CertificateRepository;
import com.levelup.course_service.repository.CourseEnrollmentRepository;
import com.levelup.course_service.repository.CourseRepository;
import com.levelup.course_service.repository.LessonRepository;
import com.levelup.course_service.service.CertificateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class CertificateServiceImpl implements CertificateService {

    @Value("${certifier.issueUrl}")
    private String certifierUrl;

    @Value("${certifier.validateUrl}")
    private String certifierValidateUrl;

    @Value("${certifier.apiKey}")
    private String apiKey;

    @Value("${certifier.groupId}")
    private String groupId;

    @Value("${certifier.version}")
    private String certifierVersion;

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final CourseEnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final WebClient webClient;
    private final UserServiceClient userServiceClient;

    public String requestCertificate(UUID enrollmentId, UUID userId) {
        CourseEnrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new RuntimeException("Enrollment not found"));

        if (!enrollment.getUserId().equals(userId)) {
            throw new RuntimeException("User not authorized for this enrollment");
        }

        if (certificateRepository.existsByEnrollmentId(enrollmentId)) {
            Certificate existingCert = certificateRepository.findByEnrollmentId(enrollmentId)
                    .orElseThrow(() -> new RuntimeException("Certificate not found"));
            return existingCert.getCertificateUrl();
        }

        if (enrollment.getStatus() != CourseEnrollment.Status.COMPLETED) {
            throw new RuntimeException("Course not completed yet");
        }

        ResponseEntity<String> response = issueCertificate(enrollment);

        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to request certificate");
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode json = mapper.readTree(response.getBody());
            String publicId = json.get("publicId").asText();

            Certificate certificate = Certificate.builder()
                    .enrollment(enrollment)
                    .publicId(publicId)
                    .certificateUrl(certifierValidateUrl + publicId)
                    .build();

            certificateRepository.save(certificate);

            return certificate.getCertificateUrl();

        } catch (Exception e) {
            throw new RuntimeException("Error parsing certificate response", e);
        }
    }

    @Override
    public List<CertificateDTO> getCertificatesByUser(UUID currentUserId) {
        List<CourseEnrollment> enrollments = enrollmentRepository.findByUserId(currentUserId);
        List<Certificate> certificates = certificateRepository.findByEnrollmentIn(enrollments);
        return certificates.stream()
                .map(this::getCertificateDTO)
                .toList();
    }

    private CertificateDTO getCertificateDTO(Certificate cert) {
        Course course = courseRepository.findById(cert.getEnrollment().getCourseId())
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return CertificateDTO.builder()
                .courseTitle(course.getTitle())
                .publicId(cert.getPublicId())
                .certificateUrl(cert.getCertificateUrl())
                .build();
    }

    private ResponseEntity<String> issueCertificate(CourseEnrollment enrollment) {
        try {
            UUID userId = enrollment.getUserId();
            UUID courseId = enrollment.getCourseId();

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));

            UserDTO user = userServiceClient.getUserData(userId);

            Integer lessonCount = lessonRepository.countByCourseId(courseId);
            String courseTitle = course.getTitle();
            Integer courseDuration = course.getDuration();
            String recipientName = user.getLastName() + " " + user.getFirstName();
            String recipientEmail = user.getEmail();

            Map<String, Object> payload = buildPayload(
                    recipientName,
                    recipientEmail,
                    courseTitle,
                    courseDuration,
                    lessonCount
            );
            log.info("Issuing certificate with payload: {}", payload);

            var response = webClient.post()
                    .uri(certifierUrl)
                    .headers(h -> {
                        h.setContentType(MediaType.APPLICATION_JSON);
                        h.setBearerAuth(apiKey);
                        h.set("Certifier-Version", certifierVersion);
                        h.set("accept", "application/json");
                    })
                    .bodyValue(payload)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext(errorBody -> log.error("4xx error response: {}", errorBody))
                                .then(clientResponse.createError());
                    })
                    .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> {
                        return clientResponse.bodyToMono(String.class)
                                .doOnNext(errorBody -> log.error("5xx error response: {}", errorBody))
                                .then(clientResponse.createError());
                    })
                    .toEntity(String.class)
                    .block();

            log.info("Issue certificate response: {}", response);
            if (response != null && response.getStatusCode().is2xxSuccessful()) {
                log.info("Certificate issued for enrollment {} (user {}, course {})",
                        enrollment.getId(), enrollment.getUserId(), enrollment.getCourseId());
            } else {
                log.warn("Failed to issue certificate. Status: {}, Body: {}",
                        response != null ? response.getStatusCode() : "null",
                        response != null ? response.getBody() : "null");
            }

            return response;

        } catch (Exception ex) {
            log.error("Error issuing certificate for enrollment {}: {}", enrollment.getId(), ex.getMessage(), ex);
            throw new RuntimeException("Error issuing certificate", ex);
        }
    }

    private Map<String, Object> buildPayload(
            String recipientName,
            String recipientEmail,
            String courseTitle,
            Integer durationHours,
            Integer lessonCount
    ) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, String> recipient = new HashMap<>();
        Map<String, String> customAttributes = new HashMap<>();

        recipient.put("name", recipientName);
        recipient.put("email", recipientEmail);

        customAttributes.put("custom.duration", String.valueOf(durationHours));
        customAttributes.put("custom.title", courseTitle);
        customAttributes.put("custom.lessons", String.valueOf(lessonCount));

        payload.put("recipient", recipient);
        payload.put("customAttributes", customAttributes);
        payload.put("groupId", groupId);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        payload.put("issueDate", LocalDate.now().format(formatter));

        return payload;
    }
}
