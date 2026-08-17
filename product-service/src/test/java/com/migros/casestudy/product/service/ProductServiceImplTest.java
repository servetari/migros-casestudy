package com.migros.casestudy.product.service;

import com.migros.casestudy.product.client.BarcodeClient;
import com.migros.casestudy.product.client.response.BarcodeResponse;

import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.dto.response.ProductResponse;
import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.ProductBarcode;
import com.migros.casestudy.product.entity.enums.UnitType;
import com.migros.casestudy.product.entity.enums.BarcodeType;
import com.migros.casestudy.product.exception.NotFoundException;
import com.migros.casestudy.product.mapper.ProductMapper;
import com.migros.casestudy.product.repository.ProductRepository;
import com.migros.casestudy.product.service.impl.ProductServiceImpl;
import com.migros.casestudy.product.validation.ProductValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private BarcodeClient barcodeClient;

    @Mock
    private ProductResponse productResponse;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(
                productRepository,
                productValidator,
                productMapper,
                barcodeClient
        );
    }

    @Test
    void create_shouldReturnCreatedProduct() {
        CreateProductRequest request = validCreateRequest();
        Product product = validProduct();

        doNothing()
                .when(productValidator)
                .validateForCreate(request);

        when(productMapper.toEntity(request))
                .thenReturn(product);

        when(productRepository.save(product))
                .thenReturn(product);

        when(barcodeClient.generateDefault(product))
                .thenReturn(new BarcodeResponse(1L, "000000001", BarcodeType.PRODUCT, 1L));

        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        ProductResponse result = productService.create(request);

        assertSame(productResponse, result);

        verify(productValidator).validateForCreate(request);
        verify(productMapper).toEntity(request);
        verify(productRepository).save(product);
        verify(barcodeClient).generateDefault(product);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getById_shouldReturnProduct_whenProductExists() {
        Long productId = 1L;
        Product product = validProduct();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        when(productMapper.toResponse(product))
                .thenReturn(productResponse);

        ProductResponse result = productService.getById(productId);

        assertSame(productResponse, result);

        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);
    }

    @Test
    void getById_shouldThrowNotFoundException_whenProductDoesNotExist() {
        Long productId = 99L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.getById(productId)
        );

        verify(productRepository).findById(productId);
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void getAll_shouldReturnAllProducts() {
        Product firstProduct = validProduct();
        Product secondProduct = secondProduct();

        ProductResponse firstResponse =
                org.mockito.Mockito.mock(ProductResponse.class);

        ProductResponse secondResponse =
                org.mockito.Mockito.mock(ProductResponse.class);

        when(productRepository.findAll())
                .thenReturn(List.of(firstProduct, secondProduct));

        when(productMapper.toResponse(firstProduct))
                .thenReturn(firstResponse);

        when(productMapper.toResponse(secondProduct))
                .thenReturn(secondResponse);

        List<ProductResponse> result = productService.getAll();

        assertEquals(2, result.size());
        assertSame(firstResponse, result.get(0));
        assertSame(secondResponse, result.get(1));

        verify(productRepository).findAll();
        verify(productMapper).toResponse(firstProduct);
        verify(productMapper).toResponse(secondProduct);
    }

    @Test
    void getAll_shouldReturnEmptyList_whenNoProductExists() {
        when(productRepository.findAll())
                .thenReturn(List.of());

        List<ProductResponse> result = productService.getAll();

        assertEquals(0, result.size());

        verify(productRepository).findAll();
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void update_shouldReturnUpdatedProduct_whenProductExists() {
        Long productId = 1L;
        UpdateProductRequest request = validUpdateRequest();
        Product existingProduct = validProduct();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));

        doNothing()
                .when(productValidator)
                .validateForUpdate(productId, request);

        /*
         * Sende updateEntity parametre sırası:
         * Product, UpdateProductRequest
         */
        doNothing()
                .when(productMapper)
                .updateEntity(existingProduct, request);

        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        when(barcodeClient.generateDefault(existingProduct))
                .thenReturn(new BarcodeResponse(2L, "000000002", BarcodeType.PRODUCT, productId));

        when(productMapper.toResponse(existingProduct))
                .thenReturn(productResponse);

        ProductResponse result =
                productService.update(productId, request);

        assertSame(productResponse, result);

        verify(productRepository).findById(productId);
        verify(productValidator).validateForUpdate(productId, request);
        verify(productMapper).updateEntity(existingProduct, request);
        verify(barcodeClient).generateDefault(existingProduct);
        verify(productRepository).save(existingProduct);
        verify(productMapper).toResponse(existingProduct);
    }

    @Test
    void update_shouldReplaceBarcodes_whenUnitChangesFromKilogramToPiece() {
        Long productId = 1L;
        UpdateProductRequest request = validUpdateRequest();
        request.setName("Levrek");
        request.setCode("BL123");
        request.setUnit(UnitType.ADET);
        request.setCategoryCode("BL");

        Product existingProduct = validProduct();
        existingProduct.setName("Balık");
        existingProduct.setCode("BL123");
        existingProduct.setCategoryCode("BL");
        existingProduct.addBarcode(new ProductBarcode("old-product", BarcodeType.PRODUCT));
        existingProduct.addBarcode(new ProductBarcode("old-scale", BarcodeType.SCALE));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        doNothing()
                .when(productValidator)
                .validateForUpdate(productId, request);
        org.mockito.Mockito.doAnswer(invocation -> {
            existingProduct.setName(request.getName());
            existingProduct.setCode(request.getCode());
            existingProduct.setBrand(request.getBrand());
            existingProduct.setUnit(request.getUnit());
            existingProduct.setCategoryCode(request.getCategoryCode());
            return null;
        }).when(productMapper).updateEntity(existingProduct, request);
        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);
        when(barcodeClient.generateDefault(existingProduct))
                .thenReturn(new BarcodeResponse(2L, "new-case", BarcodeType.CASE, productId));
        when(productMapper.toResponse(existingProduct))
                .thenReturn(productResponse);

        ProductResponse result = productService.update(productId, request);

        assertSame(productResponse, result);
        assertEquals(1, existingProduct.getBarcodes().size());
        assertEquals("new-case", existingProduct.getBarcodes().get(0).getCode());
        assertEquals(BarcodeType.CASE, existingProduct.getBarcodes().get(0).getType());
        verify(barcodeClient).generateDefault(existingProduct);
    }

    @Test
    void update_shouldKeepBarcodes_whenBarcodeContextDoesNotChange() {
        Long productId = 1L;
        UpdateProductRequest request = validUpdateRequest();
        request.setCode("ME123");
        Product existingProduct = validProduct();
        existingProduct.addBarcode(new ProductBarcode("existing", BarcodeType.PRODUCT));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        doNothing()
                .when(productValidator)
                .validateForUpdate(productId, request);
        doNothing()
                .when(productMapper)
                .updateEntity(existingProduct, request);
        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);
        when(productMapper.toResponse(existingProduct))
                .thenReturn(productResponse);

        ProductResponse result = productService.update(productId, request);

        assertSame(productResponse, result);
        assertEquals(1, existingProduct.getBarcodes().size());
        assertEquals("existing", existingProduct.getBarcodes().get(0).getCode());
        verify(barcodeClient, never()).generateDefault(any(Product.class));
    }

    @Test
    void update_shouldReplaceInvalidBarcodes_whenProductIsAlreadyPieceBased() {
        Long productId = 1L;
        UpdateProductRequest request = new UpdateProductRequest();
        request.setName("Palamut Balığı");
        request.setCode("BL003");
        request.setBrand("MIGROS");
        request.setUnit(UnitType.ADET);
        request.setCategoryCode("BL");

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Palamut Balığı");
        existingProduct.setCode("BL003");
        existingProduct.setBrand("MIGROS");
        existingProduct.setUnit(UnitType.ADET);
        existingProduct.setCategoryCode("BL");
        existingProduct.addBarcode(new ProductBarcode("old-product", BarcodeType.PRODUCT));
        existingProduct.addBarcode(new ProductBarcode("old-scale", BarcodeType.SCALE));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        doNothing()
                .when(productValidator)
                .validateForUpdate(productId, request);
        doNothing()
                .when(productMapper)
                .updateEntity(existingProduct, request);
        when(barcodeClient.generateDefault(existingProduct))
                .thenReturn(new BarcodeResponse(3L, "new-case", BarcodeType.CASE, productId));
        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);
        when(productMapper.toResponse(existingProduct))
                .thenReturn(productResponse);

        productService.update(productId, request);

        assertEquals(1, existingProduct.getBarcodes().size());
        assertEquals("new-case", existingProduct.getBarcodes().get(0).getCode());
        assertEquals(BarcodeType.CASE, existingProduct.getBarcodes().get(0).getType());
        verify(barcodeClient).generateDefault(existingProduct);
    }

    @Test
    void update_shouldThrowNotFoundException_whenProductDoesNotExist() {
        Long productId = 99L;
        UpdateProductRequest request = validUpdateRequest();

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.update(productId, request)
        );

        verify(productRepository).findById(productId);

        verify(productValidator, never())
                .validateForUpdate(productId, request);

        verify(productMapper, never())
                .updateEntity(any(Product.class), any(UpdateProductRequest.class));

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void delete_shouldDeleteProduct_whenProductExists() {
        Long productId = 1L;
        Product product = validProduct();

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        productService.delete(productId);

        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenProductDoesNotExist() {
        Long productId = 99L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> productService.delete(productId)
        );

        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any(Product.class));
    }

    private CreateProductRequest validCreateRequest() {
        CreateProductRequest request = new CreateProductRequest();

        request.setName("Elma");
        request.setCode("ME123");
        request.setBrand("Migros");
        request.setUnit(UnitType.KILOGRAM);
        request.setCategoryCode("ME");

        return request;
    }

    private UpdateProductRequest validUpdateRequest() {
        UpdateProductRequest request = new UpdateProductRequest();

        request.setName("Kırmızı Elma");
        request.setCode("ME124");
        request.setBrand("Migros");
        request.setUnit(UnitType.KILOGRAM);
        request.setCategoryCode("ME");

        return request;
    }

    private Product validProduct() {
        Product product = new Product();

        product.setId(1L);
        product.setName("Elma");
        product.setCode("ME123");
        product.setBrand("Migros");
        product.setUnit(UnitType.KILOGRAM);
        product.setCategoryCode("ME");

        return product;
    }

    private Product secondProduct() {
        Product product = new Product();

        product.setId(2L);
        product.setName("Mercimek");
        product.setCode("BK123");
        product.setBrand("Migros");
        product.setUnit(UnitType.KILOGRAM);
        product.setCategoryCode("BK");

        return product;
    }
}