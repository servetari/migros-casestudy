package com.migros.casestudy.product.controller;

import com.migros.casestudy.product.dto.request.CreateProductRequest;
import com.migros.casestudy.product.dto.request.UpdateProductRequest;
import com.migros.casestudy.product.dto.request.GenerateBarcodeRequest;
import com.migros.casestudy.product.dto.response.ProductResponse;
import com.migros.casestudy.product.service.ProductService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Ürün CRUD ve barkod ilişkilendirme işlemleri")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Ürün oluştur", description = "Ürünü kaydeder ve kategori/birim kuralına göre varsayılan barkodu bağlar.")
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response =
                productService.create(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Ürünleri listele")
    public ResponseEntity<List<ProductResponse>> getAll() {
        List<ProductResponse> response =
                productService.getAll();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ürün detayı getir")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        ProductResponse response =
                productService.getById(id);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Ürünü güncelle")
    public ResponseEntity<ProductResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        ProductResponse response =
                productService.update(id, request);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Ürünü sil")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/barcodes")
    @Operation(summary = "Ürüne barkod ekle", description = "Ürün için izin verilen barkod tiplerinden birini üretir ve ürüne bağlar.")
    public ResponseEntity<ProductResponse> addBarcode(@PathVariable Long id, @Valid @RequestBody GenerateBarcodeRequest request) {
        return ResponseEntity.ok(productService.addBarcode(id, request.type()));
    }
}