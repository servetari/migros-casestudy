package com.migros.casestudy.barcode.dto.response;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;

public record BarcodeResponse(
        Long id,
        String code,
        BarcodeType type,
        Long productId
) {
}