package com.migros.casestudy.barcode.dto.request;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.entity.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateBarcodeRequest(
        @NotNull(message = "Ürün id zorunludur.")
        Long productId,
        @NotBlank(message = "Ürün kodu zorunludur.")
        String productCode,
        @NotBlank(message = "Kategori kodu zorunludur.")
        String categoryCode,
        @NotBlank(message = "Kategori adı zorunludur.")
        String categoryName,
        @NotNull(message = "Birim zorunludur.")
        UnitType unit,
        @NotNull(message = "Barkod tipi zorunludur.")
        BarcodeType type
) {
}