package com.migros.casestudy.product.client.response;

import com.migros.casestudy.product.entity.enums.BarcodeType;

public record BarcodeResponse(
        Long id,
        String code,
        BarcodeType type,
        Long productId
) {
}