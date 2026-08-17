package com.migros.casestudy.product.controller;

import tools.jackson.databind.json.JsonMapper;
import com.migros.casestudy.product.client.BarcodeClient;
import com.migros.casestudy.product.client.response.BarcodeResponse;
import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.enums.UnitType;
import com.migros.casestudy.product.entity.enums.BarcodeType;
import com.migros.casestudy.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.migros.casestudy.product.client.CategoryClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
class ProductControllerTest {
    @MockitoBean
    private CategoryClient categoryClient;

    @MockitoBean
    private BarcodeClient barcodeClient;

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
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        when(barcodeClient.generateDefault(any(Product.class)))
                .thenReturn(new BarcodeResponse(1L, "000000001", BarcodeType.PRODUCT, 1L));
    }

    @Test
    void create_shouldReturnCreatedProduct() throws Exception {
        CreateProductRequest request = validCreateRequest();

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Elma"))
                .andExpect(jsonPath("$.code").value("ME123"))
                .andExpect(jsonPath("$.brand").value("Migros"))
                .andExpect(jsonPath("$.unit").value("KILOGRAM"))
                .andExpect(jsonPath("$.categoryCode").value("ME"));
    }

    @Test
    void openApi_shouldDescribeProductEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Product Service API"))
                .andExpect(jsonPath("$.paths['/api/v1/products']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/products/{id}/barcodes']").exists());
    }

    @Test
    void create_shouldReturnBadRequest_whenNameIsBlank()
            throws Exception {

        CreateProductRequest request = validCreateRequest();
        request.setName("");

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequest_whenCodeLengthIsInvalid()
            throws Exception {

        CreateProductRequest request = validCreateRequest();
        request.setCode("ME12");

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnBadRequest_whenCodeDoesNotMatchCategory()
            throws Exception {

        CreateProductRequest request = validCreateRequest();
        request.setCode("BK123");
        request.setCategoryCode("ME");

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_shouldReturnClientError_whenNameAlreadyExists()
            throws Exception {

        productRepository.saveAndFlush(validProduct());

        CreateProductRequest request = validCreateRequest();
        request.setCode("ME124");

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void create_shouldReturnClientError_whenCodeAlreadyExists()
            throws Exception {

        productRepository.saveAndFlush(validProduct());

        CreateProductRequest request = validCreateRequest();
        request.setName("Armut");

        mockMvc.perform(
                        post("/api/v1/products")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getAll_shouldReturnProducts() throws Exception {
        productRepository.saveAndFlush(validProduct());
        productRepository.saveAndFlush(secondProduct());

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void getAll_shouldReturnEmptyList_whenProductDoesNotExist()
            throws Exception {

        mockMvc.perform(get("/api/v1/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void getById_shouldReturnProduct_whenProductExists()
            throws Exception {

        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        mockMvc.perform(
                        get(
                                "/api/v1/products/{id}",
                                savedProduct.getId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(savedProduct.getId())
                )
                .andExpect(jsonPath("$.name").value("Elma"))
                .andExpect(jsonPath("$.code").value("ME123"))
                .andExpect(jsonPath("$.categoryCode").value("ME"));
    }

    @Test
    void getById_shouldReturnNotFound_whenProductDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        get("/api/v1/products/{id}", 99999L)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void update_shouldReturnUpdatedProduct()
            throws Exception {

        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        UpdateProductRequest request = validUpdateRequest();

        mockMvc.perform(
                        put(
                                "/api/v1/products/{id}",
                                savedProduct.getId()
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id").value(savedProduct.getId())
                )
                .andExpect(
                        jsonPath("$.name").value("Kırmızı Elma")
                )
                .andExpect(jsonPath("$.code").value("ME124"))
                .andExpect(jsonPath("$.brand").value("Migros"))
                .andExpect(jsonPath("$.unit").value("KILOGRAM"))
                .andExpect(jsonPath("$.categoryCode").value("ME"));
    }

    @Test
    void update_shouldReturnNotFound_whenProductDoesNotExist()
            throws Exception {

        UpdateProductRequest request = validUpdateRequest();

        mockMvc.perform(
                        put("/api/v1/products/{id}", 99999L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_shouldRemoveProduct()
            throws Exception {

        Product savedProduct =
                productRepository.saveAndFlush(validProduct());

        Long productId = savedProduct.getId();

        mockMvc.perform(
                        delete(
                                "/api/v1/products/{id}",
                                productId
                        )
                )
                .andExpect(status().is2xxSuccessful());

        assertFalse(productRepository.existsById(productId));
    }

    @Test
    void delete_shouldReturnNotFound_whenProductDoesNotExist()
            throws Exception {

        mockMvc.perform(
                        delete("/api/v1/products/{id}", 99999L)
                )
                .andExpect(status().isNotFound());
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

    private Product validProduct() {
        Product product = new Product();

        product.setName("Elma");
        product.setCode("ME123");
        product.setBrand("Migros");
        product.setUnit(UnitType.KILOGRAM);
        product.setCategoryCode("ME");

        return product;
    }

    private Product secondProduct() {
        Product product = new Product();

        product.setName("Mercimek");
        product.setCode("BK123");
        product.setBrand("Migros");
        product.setUnit(UnitType.KILOGRAM);
        product.setCategoryCode("BK");

        return product;
    }
}