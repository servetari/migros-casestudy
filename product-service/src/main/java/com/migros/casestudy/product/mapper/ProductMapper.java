package com.migros.casestudy.product.mapper;

import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.dto.response.ProductResponse;
import com.migros.casestudy.product.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(CreateProductRequest request) {
        Product product = new Product();

        product.setName(request.getName());
        product.setCode(request.getCode());
        product.setBrand(request.getBrand());
        product.setUnit(request.getUnit());
        product.setCategoryCode(request.getCategoryCode());

        return product;
    }

    public ProductResponse toResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCode(),
                product.getBrand(),
                product.getUnit(),
                product.getCategoryCode(),
                product.getBarcodes().stream()
                        .map(barcode -> new com.migros.casestudy.product.dto.response.ProductBarcodeResponse(
                                barcode.getCode(), barcode.getType()
                        ))
                        .toList()
        );
    }

    public void updateEntity(Product product, UpdateProductRequest request) {
        product.setName(request.getName());
        product.setCode(request.getCode());
        product.setBrand(request.getBrand());
        product.setUnit(request.getUnit());
        product.setCategoryCode(request.getCategoryCode());
    }
}