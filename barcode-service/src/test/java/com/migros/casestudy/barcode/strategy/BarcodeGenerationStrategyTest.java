package com.migros.casestudy.barcode.strategy;

import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.entity.enums.UnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BarcodeGenerationStrategyTest {
    private final ProductBarcodeContext context = new ProductBarcodeContext(
            1L, "ME123", "ME", "Meyve", UnitType.KILOGRAM
    );

    @Test
    void productStrategy_shouldGenerateNineCharacters() {
        assertEquals("000000007", new ProductBarcodeGenerationStrategy().generate(context, 7));
    }

    @Test
    void scaleStrategy_shouldUseProductCodeAndPaddedSequence() {
        assertEquals("ME123007", new ScaleBarcodeGenerationStrategy().generate(context, 7));
    }

    @Test
    void caseStrategy_shouldGenerateFourCharacters() {
        assertEquals("0007", new CaseBarcodeGenerationStrategy().generate(context, 7));
    }

    @Test
    void scaleStrategy_shouldRejectSequenceOverflow() {
        assertThrows(IllegalArgumentException.class,
                () -> new ScaleBarcodeGenerationStrategy().generate(context, 1000));
    }
}