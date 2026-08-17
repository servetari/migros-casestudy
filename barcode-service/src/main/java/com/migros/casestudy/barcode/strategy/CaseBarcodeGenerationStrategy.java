package com.migros.casestudy.barcode.strategy;

import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.exception.BarcodeValidationException;
import org.springframework.stereotype.Component;

@Component
public class CaseBarcodeGenerationStrategy implements BarcodeGenerationStrategy {
    @Override
    public BarcodeType supports() {
        return BarcodeType.CASE;
    }

    @Override
    public int maxSequence() {
        return 9_999;
    }

    @Override
    public String generate(ProductBarcodeContext context, int sequence) {
        if (sequence < 1 || sequence > maxSequence()) {
            throw new BarcodeValidationException(
                    "Kasa barkodu sequence değeri 1-9999 arasında olmalıdır."
            );
        }
        return "%04d".formatted(sequence);
    }
}