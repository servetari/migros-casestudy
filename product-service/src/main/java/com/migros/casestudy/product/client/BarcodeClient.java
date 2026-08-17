package com.migros.casestudy.product.client;

import com.migros.casestudy.product.client.response.BarcodeResponse;
import com.migros.casestudy.product.client.response.BarcodeErrorResponse;
import com.migros.casestudy.product.client.response.CategoryResponse;
import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.enums.BarcodeType;
import com.migros.casestudy.product.entity.enums.UnitType;
import com.migros.casestudy.product.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.json.JsonMapper;

import java.util.Locale;

@Component
public class BarcodeClient {
    private final RestClient restClient;
    private final CategoryClient categoryClient;
    private final JsonMapper jsonMapper;

    public BarcodeClient(
            @Value("${clients.barcode-service.url}")
            String barcodeServiceUrl,
            CategoryClient categoryClient,
            JsonMapper jsonMapper
    ) {
        this.restClient = RestClient.create(barcodeServiceUrl);
        this.categoryClient = categoryClient;
        this.jsonMapper = jsonMapper;
    }

    public BarcodeResponse generateDefault(Product product) {
        CategoryResponse category = categoryClient.getByCode(product.getCategoryCode());
        BarcodeType type = defaultType(category.name(), product.getUnit());
        return generate(product, type, category);
    }

    public BarcodeResponse generate(Product product, BarcodeType type) {
        CategoryResponse category = categoryClient.getByCode(product.getCategoryCode());
        return generate(product, type, category);
    }

    private BarcodeResponse generate(Product product, BarcodeType type, CategoryResponse category) {
        GenerateBarcodeRequest request = new GenerateBarcodeRequest(
                product.getId(), product.getCode(), product.getCategoryCode(),
                category.name(), product.getUnit(), type
        );
        try {
            BarcodeResponse response = restClient.post()
                    .uri("/api/v1/barcodes")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (requestMessage, responseMessage) -> {
                        throw new BusinessException(readErrorMessage(responseMessage));
                    })
                    .body(BarcodeResponse.class);
            if (response == null) {
                throw new BusinessException("Barkod servisinden geçerli cevap alınamadı.");
            }
            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException("Barkod servisine ulaşılamadı.");
        }
    }

    private String readErrorMessage(org.springframework.http.client.ClientHttpResponse response) {
        try {
            BarcodeErrorResponse error = jsonMapper.readValue(response.getBody(), BarcodeErrorResponse.class);
            if (error != null && error.message() != null && !error.message().isBlank()) {
                return error.message();
            }
        } catch (Exception ignored) {
            // The fallback below keeps the client error useful when the remote response is malformed.
        }
        return "Barkod servisinde hata oluştu.";
    }

    private BarcodeType defaultType(String categoryName, UnitType unit) {
        String normalizedCategory = categoryName.toLowerCase(Locale.ROOT);
        if (normalizedCategory.equals("balık") && unit == UnitType.ADET) {
            return BarcodeType.CASE;
        }
        if (normalizedCategory.equals("et")) {
            return BarcodeType.SCALE;
        }
        return BarcodeType.PRODUCT;
    }

    private record GenerateBarcodeRequest(
            Long productId,
            String productCode,
            String categoryCode,
            String categoryName,
            com.migros.casestudy.product.entity.enums.UnitType unit,
            BarcodeType type
    ) {
    }
}