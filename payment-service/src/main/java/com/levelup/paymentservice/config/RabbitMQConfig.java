package com.levelup.paymentservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange names
    public static final String PAYMENT_EXCHANGE = "payment.exchange";
    public static final String COURSE_EXCHANGE = "course.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    // Queue names
    public static final String PAYMENT_COMPLETED_QUEUE = "payment.completed.queue";
    public static final String SUBSCRIPTION_CREATED_QUEUE = "subscription.created.queue";
    public static final String REFUND_PROCESSED_QUEUE = "refund.processed.queue";
    public static final String COURSE_ACCESS_GRANTED_QUEUE = "course.access.granted.queue";
    public static final String COURSE_ACCESS_REVOKED_QUEUE = "course.access.revoked.queue";
    public static final String PAYMENT_NOTIFICATION_QUEUE = "payment.notification.queue";

    // Routing keys
    public static final String PAYMENT_COMPLETED_KEY = "payment.completed";
    public static final String SUBSCRIPTION_CREATED_KEY = "subscription.created";
    public static final String SUBSCRIPTION_CANCELLED_KEY = "subscription.cancelled";
    public static final String REFUND_PROCESSED_KEY = "refund.processed";
    public static final String COURSE_ACCESS_GRANTED_KEY = "course.access.granted";
    public static final String COURSE_ACCESS_REVOKED_KEY = "course.access.revoked";
    public static final String PAYMENT_NOTIFICATION_KEY = "payment.notification";

    // Exchanges
    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(PAYMENT_EXCHANGE);
    }

    @Bean
    public TopicExchange courseExchange() {
        return new TopicExchange(COURSE_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    // Queues
    @Bean
    public Queue paymentCompletedQueue() {
        return QueueBuilder.durable(PAYMENT_COMPLETED_QUEUE).build();
    }

    @Bean
    public Queue subscriptionCreatedQueue() {
        return QueueBuilder.durable(SUBSCRIPTION_CREATED_QUEUE).build();
    }

    @Bean
    public Queue refundProcessedQueue() {
        return QueueBuilder.durable(REFUND_PROCESSED_QUEUE).build();
    }

    @Bean
    public Queue courseAccessGrantedQueue() {
        return QueueBuilder.durable(COURSE_ACCESS_GRANTED_QUEUE).build();
    }

    @Bean
    public Queue courseAccessRevokedQueue() {
        return QueueBuilder.durable(COURSE_ACCESS_REVOKED_QUEUE).build();
    }

    @Bean
    public Queue paymentNotificationQueue() {
        return QueueBuilder.durable(PAYMENT_NOTIFICATION_QUEUE).build();
    }

    // Bindings
    @Bean
    public Binding paymentCompletedBinding() {
        return BindingBuilder.bind(paymentCompletedQueue())
                .to(paymentExchange())
                .with(PAYMENT_COMPLETED_KEY);
    }

    @Bean
    public Binding subscriptionCreatedBinding() {
        return BindingBuilder.bind(subscriptionCreatedQueue())
                .to(paymentExchange())
                .with(SUBSCRIPTION_CREATED_KEY);
    }

    @Bean
    public Binding refundProcessedBinding() {
        return BindingBuilder.bind(refundProcessedQueue())
                .to(paymentExchange())
                .with(REFUND_PROCESSED_KEY);
    }

    @Bean
    public Binding courseAccessGrantedBinding() {
        return BindingBuilder.bind(courseAccessGrantedQueue())
                .to(courseExchange())
                .with(COURSE_ACCESS_GRANTED_KEY);
    }

    @Bean
    public Binding courseAccessRevokedBinding() {
        return BindingBuilder.bind(courseAccessRevokedQueue())
                .to(courseExchange())
                .with(COURSE_ACCESS_REVOKED_KEY);
    }

    @Bean
    public Binding paymentNotificationBinding() {
        return BindingBuilder.bind(paymentNotificationQueue())
                .to(notificationExchange())
                .with(PAYMENT_NOTIFICATION_KEY);
    }
}
