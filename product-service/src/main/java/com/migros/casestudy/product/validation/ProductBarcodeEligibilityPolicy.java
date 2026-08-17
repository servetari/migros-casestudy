package com.migros.casestudy.product.validation;

import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.enums.BarcodeType;
import com.migros.casestudy.product.entity.enums.UnitType;

public final class ProductBarcodeEligibilityPolicy {

    private ProductBarcodeEligibilityPolicy() {
    }

    public static boolean isAllowed(Product product, BarcodeType type) {
        return switch (product.getCategoryCode().toUpperCase()) {
            case "ME" -> product.getUnit() == UnitType.KILOGRAM
                    ? type == BarcodeType.PRODUCT || type == BarcodeType.CASE
                    : type == BarcodeType.PRODUCT;
            case "BL" -> product.getUnit() == UnitType.KILOGRAM
                    ? type == BarcodeType.PRODUCT || type == BarcodeType.SCALE
                    : type == BarcodeType.CASE;
            case "ET" -> type == BarcodeType.SCALE;
            default -> type == BarcodeType.PRODUCT;
        };
    }

    public static BarcodeType defaultType(Product product) {
        if ("BL".equalsIgnoreCase(product.getCategoryCode())
                && product.getUnit() == UnitType.ADET) {
            return BarcodeType.CASE;
        }
        if ("ET".equalsIgnoreCase(product.getCategoryCode())) {
            return BarcodeType.SCALE;
        }
        return BarcodeType.PRODUCT;
    }
}