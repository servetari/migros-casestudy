package com.migros.casestudy.product.event;

import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.ProductBarcode;
import com.migros.casestudy.product.entity.enums.BarcodeType;
import com.migros.casestudy.product.entity.enums.UnitType;
import com.migros.casestudy.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeEventListenerTest {
    @Mock
    private ProductRepository productRepository;

    @Test
    void handle_shouldLinkBarcodeToExistingProduct() {
        Product product = product();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        new BarcodeEventListener(productRepository).handle(event());

        verify(productRepository).save(product);
    }

    @Test
    void handle_shouldNotDuplicateAnAlreadyLinkedBarcode() {
        Product product = product();
        product.addBarcode(new ProductBarcode("000000001", BarcodeType.PRODUCT));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        new BarcodeEventListener(productRepository).handle(event());

        verify(productRepository, never()).save(product);
    }

    @Test
    void handle_shouldFailWhenProductDoesNotExist() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> new BarcodeEventListener(productRepository).handle(event()));
    }

    @Test
    void handle_shouldIgnoreBarcodeThatIsInvalidForCurrentProductContext() {
        Product product = Product.builder()
                .id(1L)
                .name("Palamut Balığı")
                .code("BL003")
                .brand("MIGROS")
                .unit(UnitType.ADET)
                .categoryCode("BL")
                .barcodes(new ArrayList<>())
                .build();
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        new BarcodeEventListener(productRepository).handle(event());

        verify(productRepository, never()).save(product);
    }

    private BarcodeGeneratedEvent event() {
        return new BarcodeGeneratedEvent(10L, "000000001", BarcodeType.PRODUCT, 1L);
    }

    private Product product() {
        return Product.builder()
                .id(1L)
                .name("Elma")
                .code("ME001")
                .brand("Migros")
                .unit(UnitType.KILOGRAM)
                .categoryCode("ME")
                .barcodes(new ArrayList<>())
                .build();
    }
}