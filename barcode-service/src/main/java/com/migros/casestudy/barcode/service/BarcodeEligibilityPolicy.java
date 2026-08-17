package com.migros.casestudy.barcode.service;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.entity.enums.CategoryType;
import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.entity.enums.UnitType;
import com.migros.casestudy.barcode.exception.BarcodeValidationException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class BarcodeEligibilityPolicy {
    public void validate(ProductBarcodeContext context, BarcodeType type) {
        CategoryType category;
        try {
            category = CategoryType.from(context.categoryCode(), context.categoryName());
        } catch (IllegalArgumentException exception) {
            throw new BarcodeValidationException(
                    "Desteklenmeyen kategori: " + context.categoryCode(), exception
            );
        }

        if (context.productCode() == null || !context.productCode().matches("[A-Za-z0-9]{5}")) {
            throw new BarcodeValidationException("Ürün kodu 5 alfanümerik karakter olmalıdır.");
        }
        if (context.categoryCode() == null || !context.categoryCode().matches("[A-Za-z0-9]{2}")) {
            throw new BarcodeValidationException("Kategori kodu 2 alfanümerik karakter olmalıdır.");
        }
        if (!context.productCode().regionMatches(true, 0, context.categoryCode(), 0, 2)) {
            throw new BarcodeValidationException("Ürün kodu kategori koduyla başlamalıdır.");
        }

        Set<BarcodeType> allowed = allowedTypes(category, context.unit());
        if (!allowed.contains(type)) {
            throw new BarcodeValidationException(
                    category + " ve " + context.unit() + " için " + type
                            + " barkodu desteklenmiyor."
            );
        }
    }

    private Set<BarcodeType> allowedTypes(CategoryType category, UnitType unit) {
        return switch (category) {
            case MEYVE -> unit == UnitType.KILOGRAM
                    ? EnumSet.of(BarcodeType.PRODUCT, BarcodeType.CASE)
                    : EnumSet.of(BarcodeType.PRODUCT);
            case BALIK -> unit == UnitType.KILOGRAM
                    ? EnumSet.of(BarcodeType.PRODUCT, BarcodeType.SCALE)
                    : EnumSet.of(BarcodeType.CASE);
            case ET -> EnumSet.of(BarcodeType.SCALE);
            default -> EnumSet.of(BarcodeType.PRODUCT);
        };
    }
}