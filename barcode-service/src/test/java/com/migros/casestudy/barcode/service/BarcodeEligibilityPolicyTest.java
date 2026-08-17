package com.migros.casestudy.barcode.service;

import com.migros.casestudy.barcode.entity.ProductBarcodeContext;
import com.migros.casestudy.barcode.entity.enums.BarcodeType;
import com.migros.casestudy.barcode.entity.enums.UnitType;
import com.migros.casestudy.barcode.exception.BarcodeValidationException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BarcodeEligibilityPolicyTest {
    private final BarcodeEligibilityPolicy policy = new BarcodeEligibilityPolicy();

    @Test
    void fruitKilogram_shouldAllowProductAndCase() {
        ProductBarcodeContext context = context("ME123", "ME", "Meyve", UnitType.KILOGRAM);
        assertDoesNotThrow(() -> policy.validate(context, BarcodeType.PRODUCT));
        assertDoesNotThrow(() -> policy.validate(context, BarcodeType.CASE));
        assertThrows(BarcodeValidationException.class, () -> policy.validate(context, BarcodeType.SCALE));
    }

    @Test
    void fishKilogram_shouldAllowProductAndScale() {
        ProductBarcodeContext context = context("BL123", "BL", "Balık", UnitType.KILOGRAM);
        assertDoesNotThrow(() -> policy.validate(context, BarcodeType.PRODUCT));
        assertDoesNotThrow(() -> policy.validate(context, BarcodeType.SCALE));
        assertThrows(BarcodeValidationException.class, () -> policy.validate(context, BarcodeType.CASE));
    }

    @Test
    void fishPiece_shouldAllowCaseOnly() {
        ProductBarcodeContext context = context("BL123", "BL", "Balık", UnitType.ADET);
        assertDoesNotThrow(() -> policy.validate(context, BarcodeType.CASE));
        assertThrows(BarcodeValidationException.class, () -> policy.validate(context, BarcodeType.PRODUCT));
    }

    @Test
    void otherCategory_shouldAllowProductOnly() {
        ProductBarcodeContext context = context("BK123", "BK", "Bakliyat", UnitType.ADET);
        assertDoesNotThrow(() -> policy.validate(context, BarcodeType.PRODUCT));
        assertThrows(BarcodeValidationException.class, () -> policy.validate(context, BarcodeType.CASE));
    }

    @Test
    void policy_shouldRejectInvalidProductPrefix() {
        assertThrows(BarcodeValidationException.class,
                () -> policy.validate(context("BK123", "ME", "Meyve", UnitType.KILOGRAM), BarcodeType.PRODUCT));
    }

    private ProductBarcodeContext context(String productCode, String categoryCode, String categoryName, UnitType unit) {
        return new ProductBarcodeContext(1L, productCode, categoryCode, categoryName, unit);
    }
}