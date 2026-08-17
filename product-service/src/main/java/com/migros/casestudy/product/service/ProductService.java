package com.migros.casestudy.product.service;

import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.dto.response.ProductResponse;
import com.migros.casestudy.product.entity.enums.BarcodeType;

import java.util.List;

public interface ProductService {
    ProductResponse create(CreateProductRequest request);
    List<ProductResponse> getAll();
    ProductResponse getById(Long id);
    ProductResponse update(Long id, UpdateProductRequest request);
    ProductResponse addBarcode(Long id, BarcodeType type);
    void delete(Long id);
}
