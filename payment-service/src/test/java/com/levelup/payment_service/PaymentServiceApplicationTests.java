package com.levelup.payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:testdb",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=password",
		"spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
		"spring.rabbitmq.host=localhost",
		"spring.rabbitmq.port=5672",
		"spring.rabbitmq.username=guest",
		"spring.rabbitmq.password=guest",
		"eureka.client.enabled=false",
		"stripe.secret.key=test_key",
		"stripe.webhook.secret=test_secret",
		"course-service.base-url=http://localhost:8082",
		"user-service.base-url=http://localhost:8081"
})
class PaymentServiceApplicationTests {

	@Test
	void contextLoads() {
		// Test that the application context loads successfully
	}

}
