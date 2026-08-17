package com.migros.casestudy.barcode.entity;

import com.migros.casestudy.barcode.entity.enums.UnitType;

public record ProductBarcodeContext(
        Long productId,
        String productCode,
        String categoryCode,
        String categoryName,
        UnitType unit
) {
}