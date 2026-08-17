package com.migros.casestudy.product.event;

import com.migros.casestudy.product.entity.enums.BarcodeType;

public record BarcodeGeneratedEvent(
        Long barcodeId,
        String code,
        BarcodeType type,
        Long productId
) {
}