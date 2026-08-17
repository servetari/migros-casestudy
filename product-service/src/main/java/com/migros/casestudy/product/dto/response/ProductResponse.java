package com.migros.casestudy.product.dto.response;

import com.migros.casestudy.product.entity.enums.UnitType;

import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String code,
        String brand,
        UnitType unit,
        String categoryCode,
        List<ProductBarcodeResponse> barcodes
) {
}