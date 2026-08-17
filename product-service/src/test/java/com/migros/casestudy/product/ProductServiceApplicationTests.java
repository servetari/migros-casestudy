package com.migros.casestudy.product;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ProductServiceApplicationTests {

	@Container
	static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>("postgres:16-alpine")
					.withDatabaseName("product_test_db")
					.withUsername("test")
					.withPassword("test");

	@DynamicPropertySource
	static void configureProperties(
			DynamicPropertyRegistry registry
	) {
		registry.add(
				"spring.datasource.url",
				POSTGRES::getJdbcUrl
		);

		registry.add(
				"spring.datasource.username",
				POSTGRES::getUsername
		);

		registry.add(
				"spring.datasource.password",
				POSTGRES::getPassword
		);

		registry.add(
				"spring.datasource.driver-class-name",
				POSTGRES::getDriverClassName
		);

		registry.add(
				"spring.jpa.hibernate.ddl-auto",
				() -> "create-drop"
		);
	}

	@Test
	void contextLoads() {
	}

	@Test
	void kafkaJsonDeserializerJacksonDependencyIsAvailable() {
		assertDoesNotThrow(
				() -> Class.forName("com.fasterxml.jackson.databind.JavaType")
		);
	}
}