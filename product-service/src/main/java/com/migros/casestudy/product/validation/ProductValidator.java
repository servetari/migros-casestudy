package com.migros.casestudy.product.validation;

import com.migros.casestudy.product.client.CategoryClient;
import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.exception.BusinessException;
import com.migros.casestudy.product.repository.ProductRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductValidator {

    private final ProductRepository productRepository;
    private final CategoryClient categoryClient;

    public ProductValidator(
            ProductRepository productRepository,
            CategoryClient categoryClient
    ) {
        this.productRepository = productRepository;
        this.categoryClient = categoryClient;
    }

    public void validateForCreate(CreateProductRequest request) {
        validateProductRules(
                request.getCode(),
                request.getCategoryCode()
        );

        categoryClient.validateCategory(
                request.getCategoryCode()
        );

        if (productRepository.existsByName(
                request.getName()
        )) {
            throw new BusinessException(
                    "Bu ürün adı zaten kullanılıyor."
            );
        }

        if (productRepository.existsByCode(
                request.getCode()
        )) {
            throw new BusinessException(
                    "Bu ürün kodu zaten kullanılıyor."
            );
        }
    }

    public void validateForUpdate(Long productId, UpdateProductRequest request) {
        validateProductRules(
                request.getCode(),
                request.getCategoryCode()
        );

        categoryClient.validateCategory(
                request.getCategoryCode()
        );

        if (productRepository.existsByNameAndIdNot(
                request.getName(),
                productId
        )) {
            throw new BusinessException(
                    "Bu ürün adı başka bir ürün tarafından kullanılıyor."
            );
        }

        if (productRepository.existsByCodeAndIdNot(
                request.getCode(),
                productId
        )) {
            throw new BusinessException(
                    "Bu ürün kodu başka bir ürün tarafından kullanılıyor."
            );
        }
    }

    private void validateProductRules(String productCode, String categoryCode) {
        if (productCode == null
                || productCode.length() != 5) {
            throw new BusinessException(
                    "Ürün kodu 5 karakter olmalıdır."
            );
        }

        if (categoryCode == null
                || categoryCode.length() != 2) {
            throw new BusinessException(
                    "Kategori kodu 2 karakter olmalıdır."
            );
        }

        if (!productCode.startsWith(categoryCode)) {
            throw new BusinessException(
                    "Ürün kodu kategori koduyla başlamalıdır."
            );
        }
    }
}