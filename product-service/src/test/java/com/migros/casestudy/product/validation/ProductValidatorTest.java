package com.migros.casestudy.product.validation;

import com.migros.casestudy.product.client.CategoryClient;
import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.entity.enums.UnitType;
import com.migros.casestudy.product.exception.BusinessException;
import com.migros.casestudy.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductValidatorTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryClient categoryClient;

    private ProductValidator productValidator;

    @BeforeEach
    void setUp() {
        productValidator = new ProductValidator(
                productRepository,
                categoryClient
        );
    }

    @Test
    void validateForCreate_shouldNotThrow_whenProductIsValid() {
        CreateProductRequest request =
                validCreateRequest();

        doNothing()
                .when(categoryClient)
                .validateCategory("ME");

        when(productRepository.existsByName("Elma"))
                .thenReturn(false);

        when(productRepository.existsByCode("ME123"))
                .thenReturn(false);

        assertDoesNotThrow(
                () -> productValidator
                        .validateForCreate(request)
        );

        verify(categoryClient)
                .validateCategory("ME");
    }

    @Test
    void validateForCreate_shouldThrow_whenCategoryDoesNotExist() {
        CreateProductRequest request =
                validCreateRequest();

        org.mockito.Mockito.doThrow(
                        new BusinessException(
                                "Geçersiz kategori kodu: ME"
                        )
                )
                .when(categoryClient)
                .validateCategory("ME");

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForCreate(request)
        );
    }

    @Test
    void validateForCreate_shouldThrow_whenNameAlreadyExists() {
        CreateProductRequest request =
                validCreateRequest();

        doNothing()
                .when(categoryClient)
                .validateCategory("ME");

        when(productRepository.existsByName("Elma"))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForCreate(request)
        );
    }

    @Test
    void validateForCreate_shouldThrow_whenCodeAlreadyExists() {
        CreateProductRequest request =
                validCreateRequest();

        doNothing()
                .when(categoryClient)
                .validateCategory("ME");

        when(productRepository.existsByName("Elma"))
                .thenReturn(false);

        when(productRepository.existsByCode("ME123"))
                .thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForCreate(request)
        );
    }

    @Test
    void validateForCreate_shouldThrow_whenCodeLengthIsInvalid() {
        CreateProductRequest request =
                validCreateRequest();

        request.setCode("ME12");

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForCreate(request)
        );
    }

    @Test
    void validateForCreate_shouldThrow_whenCategoryLengthIsInvalid() {
        CreateProductRequest request =
                validCreateRequest();

        request.setCategoryCode("MEY");

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForCreate(request)
        );
    }

    @Test
    void validateForCreate_shouldThrow_whenCodeDoesNotStartWithCategory() {
        CreateProductRequest request =
                validCreateRequest();

        request.setCode("BK123");
        request.setCategoryCode("ME");

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForCreate(request)
        );
    }

    @Test
    void validateForUpdate_shouldNotThrow_whenProductIsValid() {
        Long productId = 1L;
        UpdateProductRequest request =
                validUpdateRequest();

        doNothing()
                .when(categoryClient)
                .validateCategory("ME");

        when(productRepository.existsByNameAndIdNot(
                request.getName(),
                productId
        )).thenReturn(false);

        when(productRepository.existsByCodeAndIdNot(
                request.getCode(),
                productId
        )).thenReturn(false);

        assertDoesNotThrow(
                () -> productValidator
                        .validateForUpdate(
                                productId,
                                request
                        )
        );

        verify(categoryClient)
                .validateCategory("ME");
    }

    @Test
    void validateForUpdate_shouldThrow_whenCategoryDoesNotExist() {
        Long productId = 1L;
        UpdateProductRequest request =
                validUpdateRequest();

        org.mockito.Mockito.doThrow(
                        new BusinessException(
                                "Geçersiz kategori kodu: ME"
                        )
                )
                .when(categoryClient)
                .validateCategory("ME");

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForUpdate(
                                productId,
                                request
                        )
        );
    }

    @Test
    void validateForUpdate_shouldThrow_whenNameAlreadyExists() {
        Long productId = 1L;
        UpdateProductRequest request =
                validUpdateRequest();

        doNothing()
                .when(categoryClient)
                .validateCategory("ME");

        when(productRepository.existsByNameAndIdNot(
                request.getName(),
                productId
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForUpdate(
                                productId,
                                request
                        )
        );
    }

    @Test
    void validateForUpdate_shouldThrow_whenCodeAlreadyExists() {
        Long productId = 1L;
        UpdateProductRequest request =
                validUpdateRequest();

        doNothing()
                .when(categoryClient)
                .validateCategory("ME");

        when(productRepository.existsByNameAndIdNot(
                request.getName(),
                productId
        )).thenReturn(false);

        when(productRepository.existsByCodeAndIdNot(
                request.getCode(),
                productId
        )).thenReturn(true);

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForUpdate(
                                productId,
                                request
                        )
        );
    }

    @Test
    void validateForUpdate_shouldThrow_whenCodeLengthIsInvalid() {
        UpdateProductRequest request =
                validUpdateRequest();

        request.setCode("ME12");

        assertThrows(
                BusinessException.class,
                () -> productValidator
                        .validateForUpdate(
                                1L,
                                request
                        )
        );
    }

    private CreateProductRequest validCreateRequest() {
        CreateProductRequest request =
                new CreateProductRequest();

        request.setName("Elma");
        request.setCode("ME123");
        request.setBrand("Migros");
        request.setUnit(UnitType.KILOGRAM);
        request.setCategoryCode("ME");

        return request;
    }

    private UpdateProductRequest validUpdateRequest() {
        UpdateProductRequest request =
                new UpdateProductRequest();

        request.setName("Kırmızı Elma");
        request.setCode("ME124");
        request.setBrand("Migros");
        request.setUnit(UnitType.KILOGRAM);
        request.setCategoryCode("ME");

        return request;
    }
}