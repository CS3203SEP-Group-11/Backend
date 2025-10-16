package com.levelup.course_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"eureka.client.enabled=false",
		"spring.cloud.discovery.enabled=false",
		"spring.rabbitmq.host=localhost",
		"spring.rabbitmq.port=5672"
})
class CourseServiceApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring application context loads successfully
	}

}
