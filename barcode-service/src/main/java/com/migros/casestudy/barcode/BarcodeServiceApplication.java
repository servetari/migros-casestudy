package com.migros.casestudy.barcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(
		title = "Barcode Service API",
		version = "v1",
		description = "PRODUCT, SCALE ve CASE barkodu üretim API'si"
))
public class BarcodeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarcodeServiceApplication.class, args);
	}

}
