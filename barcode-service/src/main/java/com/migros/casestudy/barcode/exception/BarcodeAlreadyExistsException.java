package com.migros.casestudy.barcode.exception;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;

public class BarcodeAlreadyExistsException extends IllegalStateException {
    public BarcodeAlreadyExistsException(Long productId, BarcodeType type) {
        super("Ürün için " + type + " tipinde barkod zaten mevcut. Ürün id: " + productId);
    }
}