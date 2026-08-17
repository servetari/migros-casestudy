package com.migros.casestudy.barcode.strategy;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.entity.ProductBarcodeContext;

public interface  BarcodeGenerationStrategy {
    BarcodeType supports();

    int maxSequence();

    String generate(ProductBarcodeContext context, int sequence);
}