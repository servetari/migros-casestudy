package com.migros.casestudy.barcode.entity.enums;

import java.util.Arrays;

public enum CategoryType {
    BAKLIYAT("BK", "Bakliyat"),
    MEYVE("ME", "Meyve"),
    ET("ET", "Et"),
    ICECEK("IC", "İçecek"),
    BALIK("BL", "Balık");

    private final String code;
    private final String name;

    CategoryType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CategoryType from(String code, String name) {
        return Arrays.stream(values())
                .filter(category -> category.code.equalsIgnoreCase(code)
                        || category.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Desteklenmeyen kategori: " + code));
    }
}