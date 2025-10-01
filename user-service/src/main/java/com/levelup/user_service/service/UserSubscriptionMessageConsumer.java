package com.levelup.user_service.service;

import com.levelup.user_service.config.RabbitMQConfig;
import com.levelup.user_service.dto.UserSubscriptionMessage;
import com.levelup.user_service.entity.User;
import com.levelup.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSubscriptionMessageConsumer {

    private final UserRepository userRepository;

    @RabbitListener(queues = RabbitMQConfig.USER_SUBSCRIPTION_QUEUE)
    @Transactional
    public void handleUserSubscriptionMessage(UserSubscriptionMessage message) {
        try {
            log.info("Received user subscription message for user: {} with isSubscribed: {} and status: {}",
                    message.getUserId(), message.isSubscribed(), message.getStatus());

            // Find and update user
            User user = userRepository.findById(message.getUserId()).orElse(null);

            if (user == null) {
                log.error("User not found with ID: {}", message.getUserId());
                return;
            }

            // Update subscription status
            user.setIsSubscribed(message.isSubscribed());
            userRepository.save(user);

            log.info("User subscription status updated successfully for user: {} - isSubscribed: {}",
                    message.getUserId(), message.isSubscribed());

        } catch (Exception e) {
            log.error("Error processing user subscription message: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger message requeue if needed
        }
    }
}