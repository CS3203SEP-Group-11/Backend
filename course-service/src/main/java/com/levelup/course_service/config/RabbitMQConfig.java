package com.levelup.course_service.config;

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
    public static final String COURSE_ENROLLMENT_EXCHANGE = "course.enrollment.exchange";

    // Queue names
    public static final String COURSE_ENROLLMENT_QUEUE = "course.enrollment.queue";

    // Routing keys
    public static final String COURSE_ENROLLMENT_ROUTING_KEY = "course.enrollment";

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

    // Course Enrollment Exchange and Queue
    @Bean
    public TopicExchange courseEnrollmentExchange() {
        return new TopicExchange(COURSE_ENROLLMENT_EXCHANGE);
    }

    @Bean
    public Queue courseEnrollmentQueue() {
        return QueueBuilder.durable(COURSE_ENROLLMENT_QUEUE).build();
    }

    @Bean
    public Binding courseEnrollmentBinding() {
        return BindingBuilder
                .bind(courseEnrollmentQueue())
                .to(courseEnrollmentExchange())
                .with(COURSE_ENROLLMENT_ROUTING_KEY);
    }
}