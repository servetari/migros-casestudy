package com.migros.casestudy.product.client;

import com.migros.casestudy.product.client.response.CategoryResponse;
import com.migros.casestudy.product.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CategoryClient {

    private final RestClient restClient;

    public CategoryClient(
            @Value("${clients.category-service.url}")
            String categoryServiceUrl
    ) {
        this.restClient = RestClient.create(categoryServiceUrl);
    }

    public CategoryResponse getByCode(String categoryCode) {
        try {
            CategoryResponse response = restClient
                    .get()
                    .uri(
                            "/api/v1/categories/{code}",
                            categoryCode
                    )
                    .retrieve()
                    .onStatus(
                            status -> status.value() == 404,
                            (request, responseError) -> {
                                throw new BusinessException(
                                        "Geçersiz kategori kodu: "
                                                + categoryCode
                                );
                            }
                    )
                    .onStatus(
                            HttpStatusCode::is5xxServerError,
                            (request, responseError) -> {
                                throw new BusinessException(
                                        "Kategori servisine şu anda ulaşılamıyor."
                                );
                            }
                    )
                    .body(CategoryResponse.class);

            if (response == null) {
                throw new BusinessException(
                        "Kategori servisinden geçerli cevap alınamadı."
                );
            }

            return response;
        } catch (BusinessException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new BusinessException(
                    "Kategori servisine ulaşılamadı."
            );
        }
    }

    public void validateCategory(String categoryCode) {
        CategoryResponse category =
                getByCode(categoryCode);

        if (!category.code().equalsIgnoreCase(categoryCode)) {
            throw new BusinessException(
                    "Kategori kodu doğrulanamadı: "
                            + categoryCode
            );
        }
    }
}