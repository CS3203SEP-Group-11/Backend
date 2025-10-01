package com.levelup.user_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String USER_SUBSCRIPTION_EXCHANGE = "user.subscription.exchange";

    // Queue names
    public static final String USER_SUBSCRIPTION_QUEUE = "user.subscription.queue";

    // Routing keys
    public static final String USER_SUBSCRIPTION_ROUTING_KEY = "user.subscription";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }

    // User Subscription Exchange and Queue
    @Bean
    public TopicExchange userSubscriptionExchange() {
        return new TopicExchange(USER_SUBSCRIPTION_EXCHANGE);
    }

    @Bean
    public Queue userSubscriptionQueue() {
        return QueueBuilder.durable(USER_SUBSCRIPTION_QUEUE).build();
    }

    @Bean
    public Binding userSubscriptionBinding() {
        return BindingBuilder
                .bind(userSubscriptionQueue())
                .to(userSubscriptionExchange())
                .with(USER_SUBSCRIPTION_ROUTING_KEY);
    }
}