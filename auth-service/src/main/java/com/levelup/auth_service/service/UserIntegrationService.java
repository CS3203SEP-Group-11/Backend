package com.levelup.auth_service.service;

import com.levelup.auth_service.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserIntegrationService {

    private final WebClient userServiceWebClient;

    public UserDTO getUserById(String userId) {
        try {
            return userServiceWebClient.get()
                    .uri("/api/users/{userId}", userId)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse
                                    .bodyToMono(String.class)
                                    .map(body -> new RuntimeException("Failed to get user: " + body)))
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("User service call failed for getUserById", e);
        }
    }

    public void createUser(UserDTO userDTO) {
        try {
            log.info("Attempting to create user: {}", userDTO.getEmail());

            userServiceWebClient.post()
                    .uri("/api/users")
                    .bodyValue(userDTO)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> {
                                log.error("Error response status: {}", response.statusCode());
                                return response.bodyToMono(String.class)
                                        .map(body -> {
                                            log.error("Error response body: {}", body);
                                            return new RuntimeException("Failed to create user: " + body);
                                        });
                            })
                    .bodyToMono(Void.class)
                    .timeout(Duration.ofSeconds(10))
                    .block();

            log.info("Successfully created user: {}", userDTO.getEmail());
        } catch (Exception e) {
            log.error("User service call failed for createUser: {}", e.getMessage(), e);
            throw new RuntimeException("User service call failed for createUser", e);
        }
    }
}
