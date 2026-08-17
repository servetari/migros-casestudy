package com.migros.casestudy.product.dto.request;

import com.migros.casestudy.product.entity.enums.UnitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Ürün adı zorunludur.")
    private String name;

    @NotBlank(message = "Ürün kodu zorunludur.")
    @Size(
            min = 5,
            max = 5,
            message = "Ürün kodu 5 karakter olmalıdır."
    )
    private String code;

    @NotBlank(message = "Marka zorunludur.")
    private String brand;

    @NotNull(message = "Birim zorunludur.")
    private UnitType unit;

    @NotBlank(message = "Kategori kodu zorunludur.")
    @Size(
            min = 2,
            max = 2,
            message = "Kategori kodu 2 karakter olmalıdır."
    )
    private String categoryCode;
}