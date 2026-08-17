package com.migros.casestudy.product.repository;

import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.enums.UnitType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
class ProductRepositoryIT {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("product_test_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(
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

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
    }

    @Test
    void save_shouldPersistProduct() {
        Product product = validProduct();

        Product savedProduct =
                productRepository.saveAndFlush(product);

        assertNotNull(savedProduct.getId());
        assertEquals("Elma", savedProduct.getName());
        assertEquals("ME123", savedProduct.getCode());
        assertEquals("Migros", savedProduct.getBrand());
        assertEquals(
                UnitType.KILOGRAM,
                savedProduct.getUnit()
        );
        assertEquals(
                "ME",
                savedProduct.getCategoryCode()
        );
    }

    @Test
    void findById_shouldReturnProduct_whenProductExists() {
        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        Optional<Product> result =
                productRepository.findById(savedProduct.getId());

        assertTrue(result.isPresent());
        assertEquals("Elma", result.get().getName());
        assertEquals("ME123", result.get().getCode());
    }

    @Test
    void findById_shouldReturnEmpty_whenProductDoesNotExist() {
        Optional<Product> result =
                productRepository.findById(99999L);

        assertTrue(result.isEmpty());
    }

    @Test
    void existsByName_shouldReturnTrue_whenNameExists() {
        productRepository.saveAndFlush(validProduct());

        boolean result =
                productRepository.existsByName("Elma");

        assertTrue(result);
    }

    @Test
    void existsByName_shouldReturnFalse_whenNameDoesNotExist() {
        boolean result =
                productRepository.existsByName("Olmayan Ürün");

        assertFalse(result);
    }

    @Test
    void existsByCode_shouldReturnTrue_whenCodeExists() {
        productRepository.saveAndFlush(validProduct());

        boolean result =
                productRepository.existsByCode("ME123");

        assertTrue(result);
    }

    @Test
    void existsByCode_shouldReturnFalse_whenCodeDoesNotExist() {
        boolean result =
                productRepository.existsByCode("ME999");

        assertFalse(result);
    }

    @Test
    void existsByNameAndIdNot_shouldReturnFalse_forSameProduct() {
        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        boolean result =
                productRepository.existsByNameAndIdNot(
                        savedProduct.getName(),
                        savedProduct.getId()
                );

        assertFalse(result);
    }

    @Test
    void existsByNameAndIdNot_shouldReturnTrue_forAnotherId() {
        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        boolean result =
                productRepository.existsByNameAndIdNot(
                        savedProduct.getName(),
                        99999L
                );

        assertTrue(result);
    }

    @Test
    void existsByCodeAndIdNot_shouldReturnFalse_forSameProduct() {
        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        boolean result =
                productRepository.existsByCodeAndIdNot(
                        savedProduct.getCode(),
                        savedProduct.getId()
                );

        assertFalse(result);
    }

    @Test
    void existsByCodeAndIdNot_shouldReturnTrue_forAnotherId() {
        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        boolean result =
                productRepository.existsByCodeAndIdNot(
                        savedProduct.getCode(),
                        99999L
                );

        assertTrue(result);
    }

    @Test
    void delete_shouldRemoveProduct() {
        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        Long productId = savedProduct.getId();

        productRepository.delete(savedProduct);
        productRepository.flush();

        assertFalse(
                productRepository.existsById(productId)
        );
    }

    private Product validProduct() {
        Product product = new Product();

        product.setName("Elma");
        product.setCode("ME123");
        product.setBrand("Migros");
        product.setUnit(UnitType.KILOGRAM);
        product.setCategoryCode("ME");

        return product;
    }
}