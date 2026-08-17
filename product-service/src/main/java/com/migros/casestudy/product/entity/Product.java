package com.migros.casestudy.product.entity;

import com.migros.casestudy.product.entity.enums.UnitType;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_products_name",
                        columnNames = "name"
                ),
                @UniqueConstraint(
                        name = "uk_products_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 5)
    private String code;

    @Column(nullable = false)
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnitType unit;

    @Column(
            name = "category_code",
            nullable = false,
            length = 2
    )
    private String categoryCode;

    @ElementCollection
    @CollectionTable(
            name = "product_barcodes",
            joinColumns = @JoinColumn(name = "product_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_product_barcodes_code", columnNames = "code"
            )
    )
    private List<ProductBarcode> barcodes = new ArrayList<>();

    public void addBarcode(ProductBarcode barcode) {
        barcodes.add(barcode);
    }

    public void clearBarcodes() {
        barcodes.clear();
    }
}