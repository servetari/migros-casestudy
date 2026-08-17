package com.migros.casestudy.barcode.service;

import com.migros.casestudy.barcode.entity.Barcode;
import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.entity.enums.UnitType;
import com.migros.casestudy.barcode.dto.request.GenerateBarcodeRequest;
import com.migros.casestudy.barcode.dto.response.BarcodeResponse;
import com.migros.casestudy.barcode.exception.BarcodeAlreadyExistsException;
import com.migros.casestudy.barcode.exception.BarcodeSequenceExhaustedException;
import com.migros.casestudy.barcode.repository.BarcodeRepository;
import com.migros.casestudy.barcode.service.impl.BarcodeServiceImpl;
import com.migros.casestudy.barcode.strategy.BarcodeGenerationStrategy;
import com.migros.casestudy.barcode.strategy.CaseBarcodeGenerationStrategy;
import com.migros.casestudy.barcode.strategy.ProductBarcodeGenerationStrategy;
import com.migros.casestudy.barcode.strategy.ScaleBarcodeGenerationStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BarcodeServiceTest {
    @Mock
    private BarcodeRepository barcodeRepository;

    @Mock
    private BarcodeEventPublisher eventPublisher;

    private BarcodeService barcodeService;

    @BeforeEach
    void setUp() {
        List<BarcodeGenerationStrategy> strategies = List.of(
                new ProductBarcodeGenerationStrategy(),
                new ScaleBarcodeGenerationStrategy(),
                new CaseBarcodeGenerationStrategy()
        );

        barcodeService = new BarcodeServiceImpl(
                barcodeRepository,
                new BarcodeEligibilityPolicy(),
                new BarcodeStrategyFactory(strategies),
                eventPublisher
        );
    }
    @Test
    void generate_shouldPersistProductBarcode() {
        GenerateBarcodeRequest request = request(BarcodeType.PRODUCT, "ME123", "ME", "Meyve", UnitType.KILOGRAM);
        when(barcodeRepository.existsByProductIdAndType(1L, BarcodeType.PRODUCT)).thenReturn(false);
        when(barcodeRepository.findTopByTypeOrderBySequenceDesc(BarcodeType.PRODUCT)).thenReturn(Optional.empty());
        when(barcodeRepository.save(org.mockito.ArgumentMatchers.any(Barcode.class)))
                .thenReturn(Barcode.builder().id(1L).code("000000001").type(BarcodeType.PRODUCT)
                        .productId(1L).sequence(1).build());

        BarcodeResponse response = barcodeService.generate(request);

        assertEquals("000000001", response.code());
        assertEquals(BarcodeType.PRODUCT, response.type());
    }

    @Test
    void generate_shouldRejectDuplicateTypeForProduct() {
        when(barcodeRepository.existsByProductIdAndType(1L, BarcodeType.PRODUCT)).thenReturn(true);

        assertThrows(BarcodeAlreadyExistsException.class,
                () -> barcodeService.generate(request(
                        BarcodeType.PRODUCT, "ME123", "ME", "Meyve", UnitType.KILOGRAM
                )));
    }

    @Test
    void generate_shouldRejectWhenSequenceIsExhausted() {
        when(barcodeRepository.existsByProductIdAndType(1L, BarcodeType.SCALE)).thenReturn(false);
        when(barcodeRepository.findTopByTypeOrderBySequenceDesc(BarcodeType.SCALE))
                .thenReturn(Optional.of(Barcode.builder().sequence(999).build()));

        assertThrows(BarcodeSequenceExhaustedException.class,
                () -> barcodeService.generate(request(
                        BarcodeType.SCALE, "BL123", "BL", "Balık", UnitType.KILOGRAM
                )));
    }

    private GenerateBarcodeRequest request(
            BarcodeType type, String productCode, String categoryCode,
            String categoryName, UnitType unit
    ) {
        return new GenerateBarcodeRequest(
                1L, productCode, categoryCode, categoryName, unit, type
        );
    }
}