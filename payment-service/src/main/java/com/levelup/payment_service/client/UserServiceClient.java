package com.levelup.payment_service.client;

import com.levelup.payment_service.dto.external.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserServiceClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${user-service.base-url}")
    private String userServiceBaseUrl;

    public UserDto getUserById(UUID userId) {
        try {
            log.info("Fetching user details for user ID: {}", userId);

            return webClientBuilder.build()
                    .get()
                    .uri(userServiceBaseUrl + "/api/users/{userId}", userId)
                    .retrieve()
                    .bodyToMono(UserDto.class)
                    .block();

        } catch (Exception e) {
            log.error("Error fetching user details for user ID: {}", userId, e);
            throw new RuntimeException("Failed to fetch user details", e);
        }
    }

    public void updateUserStripeCustomerId(UUID userId, String stripeCustomerId) {
        try {
            log.info("Updating Stripe customer ID for user: {}", userId);

            webClientBuilder.build()
                    .put()
                    .uri(userServiceBaseUrl + "/api/users/{userId}/stripe-customer", userId)
                    .bodyValue(stripeCustomerId)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();

        } catch (Exception e) {
            log.error("Error updating Stripe customer ID for user: {}", userId, e);
            throw new RuntimeException("Failed to update Stripe customer ID", e);
        }
    }
}