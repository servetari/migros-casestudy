package com.migros.casestudy.product.dto.response;

import com.migros.casestudy.product.entity.enums.BarcodeType;

public record ProductBarcodeResponse(
        String code,
        BarcodeType type
) {
}