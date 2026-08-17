package com.migros.casestudy.product.entity;

import com.migros.casestudy.product.entity.enums.BarcodeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProductBarcode {
    private String code;

    @Enumerated(EnumType.STRING)
    private BarcodeType type;
}