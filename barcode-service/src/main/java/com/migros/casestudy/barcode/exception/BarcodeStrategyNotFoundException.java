package com.migros.casestudy.barcode.exception;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;

public class BarcodeStrategyNotFoundException extends RuntimeException {
    public BarcodeStrategyNotFoundException(BarcodeType type) {
        super(type + " barkodu için üretim stratejisi bulunamadı.");
    }
}