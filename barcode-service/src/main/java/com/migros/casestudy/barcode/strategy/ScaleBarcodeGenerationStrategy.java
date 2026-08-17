package com.migros.casestudy.barcode.strategy;

import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.exception.BarcodeValidationException;
import org.springframework.stereotype.Component;

@Component
public class ScaleBarcodeGenerationStrategy implements BarcodeGenerationStrategy {
    @Override
    public BarcodeType supports() {
        return BarcodeType.SCALE;
    }

    @Override
    public int maxSequence() {
        return 999;
    }

    @Override
    public String generate(ProductBarcodeContext context, int sequence) {
        if (context.productCode() == null || !context.productCode().matches("[A-Za-z0-9]{5}")) {
            throw new BarcodeValidationException(
                    "Terazi barkodu için ürün kodu 5 alfanümerik karakter olmalıdır."
            );
        }
        if (sequence < 1 || sequence > maxSequence()) {
            throw new BarcodeValidationException(
                    "Terazi barkodu sequence değeri 1-999 arasında olmalıdır."
            );
        }
        return context.productCode() + "%03d".formatted(sequence);
    }
}