package com.levelup.paymentservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${course.service.url}")
    private String courseServiceUrl;

    @Bean(name = "courseServiceWebClient")
    public WebClient courseServiceWebClient() {
        return WebClient.builder()
                .baseUrl(courseServiceUrl)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Bean(name = "stripeWebClient")
    public WebClient stripeWebClient() {
        return WebClient.builder()
                .baseUrl("https://api.stripe.com/v1")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
