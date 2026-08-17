package com.migros.casestudy.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Category Service API",
		version = "v1",
		description = "Kategori lookup API'si"
))
public class CategoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(
				CategoryServiceApplication.class,
				args
		);
	}
}