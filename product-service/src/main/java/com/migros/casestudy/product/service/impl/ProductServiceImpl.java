package com.migros.casestudy.product.service.impl;

import com.migros.casestudy.product.client.BarcodeClient;
import com.migros.casestudy.product.client.response.BarcodeResponse;
import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.dto.response.ProductResponse;
import com.migros.casestudy.product.entity.Product;
import com.migros.casestudy.product.entity.ProductBarcode;
import com.migros.casestudy.product.entity.enums.BarcodeType;
import com.migros.casestudy.product.mapper.ProductMapper;
import com.migros.casestudy.product.repository.ProductRepository;
import com.migros.casestudy.product.service.ProductService;
import com.migros.casestudy.product.validation.ProductBarcodeEligibilityPolicy;
import com.migros.casestudy.product.validation.ProductValidator;
import com.migros.casestudy.product.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final ProductValidator productValidator;
    private final ProductMapper productMapper;
    private final BarcodeClient barcodeClient;

    @Override
    @Transactional
    public ProductResponse create(CreateProductRequest request) {
        productValidator.validateForCreate(request);

        Product product = productMapper.toEntity(request);
        Product savedProduct = productRepository.save(product);
        addGeneratedBarcode(savedProduct, barcodeClient.generateDefault(savedProduct));

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getAll() {
        return productRepository.findAll()
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        Product product = findProductById(id);
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = findProductById(id);
        productValidator.validateForUpdate(id, request);

        boolean barcodeContextChanged = barcodeContextChanged(product, request);
        productMapper.updateEntity(product, request);
        if (barcodeContextChanged) {
            replaceBarcodesWithGeneratedDefault(product);
        } else if (hasInvalidBarcode(product)) {
            reconcileInvalidBarcodes(product);
        }
        Product updatedProduct = productRepository.save(product);

        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Product product = findProductById(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public ProductResponse addBarcode(Long id, BarcodeType type) {
        Product product = findProductById(id);
        ensureBarcodeTypeAvailable(product, type);
        addGeneratedBarcode(product, barcodeClient.generate(product, type));
        return productMapper.toResponse(productRepository.save(product));
    }

    private void addGeneratedBarcode(Product product, BarcodeResponse barcode) {
        product.addBarcode(new ProductBarcode(barcode.code(), barcode.type()));
    }

    private void replaceBarcodesWithGeneratedDefault(Product product) {
        product.clearBarcodes();
        addGeneratedBarcode(product, barcodeClient.generateDefault(product));
    }

    private void reconcileInvalidBarcodes(Product product) {
        ProductBarcode defaultBarcode = product.getBarcodes().stream()
                .filter(barcode -> barcode.getType()
                        == ProductBarcodeEligibilityPolicy.defaultType(product))
                .findFirst()
                .orElse(null);

        product.clearBarcodes();
        if (defaultBarcode != null) {
            product.addBarcode(defaultBarcode);
        } else {
            addGeneratedBarcode(product, barcodeClient.generateDefault(product));
        }
    }

    private boolean hasInvalidBarcode(Product product) {
        return product.getBarcodes().isEmpty()
                || product.getBarcodes().stream()
                .anyMatch(barcode -> !ProductBarcodeEligibilityPolicy.isAllowed(
                        product, barcode.getType()
                ));
    }

    private boolean barcodeContextChanged(Product product, UpdateProductRequest request) {
        return !Objects.equals(product.getCode(), request.getCode())
                || !Objects.equals(product.getUnit(), request.getUnit())
                || !Objects.equals(product.getCategoryCode(), request.getCategoryCode());
    }

    private void ensureBarcodeTypeAvailable(Product product,BarcodeType type) {
        if (product.getBarcodes().stream().anyMatch(barcode -> barcode.getType() == type)) {
            throw new com.migros.casestudy.product.exception.BusinessException(
                    "Bu ürün için bu tipte barkod zaten mevcut."
            );
        }
    }

    private Product findProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Ürün bulunamadı: " + id)
                );
    }
}
