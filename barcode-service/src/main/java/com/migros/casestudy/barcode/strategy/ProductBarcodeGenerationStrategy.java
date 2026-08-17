package com.migros.casestudy.barcode.strategy;

import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.exception.BarcodeValidationException;
import org.springframework.stereotype.Component;

@Component
public class ProductBarcodeGenerationStrategy implements BarcodeGenerationStrategy {
    @Override
    public BarcodeType supports() {
        return BarcodeType.PRODUCT;
    }

    @Override
    public int maxSequence() {
        return 999_999_999;
    }

    @Override
    public String generate(ProductBarcodeContext context, int sequence) {
        if (sequence < 1 || sequence > maxSequence()) {
            throw new BarcodeValidationException(
                    "Ürün barkodu sequence değeri 1-999999999 arasında olmalıdır."
            );
        }        return "%09d".formatted(sequence);
    }
}