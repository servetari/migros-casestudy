package com.migros.casestudy.product.dto.request;

import com.migros.casestudy.product.entity.enums.BarcodeType;
import jakarta.validation.constraints.NotNull;

public record GenerateBarcodeRequest(
        @NotNull(message = "Barkod tipi zorunludur.") BarcodeType type
) {
}