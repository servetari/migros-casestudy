package com.migros.casestudy.barcode.event;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;

public record BarcodeGeneratedEvent(
        Long barcodeId,
        String code,
        BarcodeType type,
        Long productId
) {
}