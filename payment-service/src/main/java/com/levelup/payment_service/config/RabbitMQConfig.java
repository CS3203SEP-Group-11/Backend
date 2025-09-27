package com.levelup.payment_service.config;

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
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Queue names
    public static final String COURSE_ENROLLMENT_QUEUE = "course.enrollment.queue";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    // Routing keys
    public static final String COURSE_ENROLLMENT_ROUTING_KEY = "course.enrollment";
    public static final String NOTIFICATION_ROUTING_KEY = "payment.notification";

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
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

    // Notification Exchange and Queue
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_ROUTING_KEY);
    }
}