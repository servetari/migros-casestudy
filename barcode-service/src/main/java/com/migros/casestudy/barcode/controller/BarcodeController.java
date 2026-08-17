package com.migros.casestudy.barcode.controller;

import com.migros.casestudy.barcode.dto.request.GenerateBarcodeRequest;
import com.migros.casestudy.barcode.dto.response.BarcodeResponse;
import com.migros.casestudy.barcode.service.BarcodeService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/barcodes")
@RequiredArgsConstructor
@Tag(name = "Barcodes", description = "Ürün bağlamına göre barkod üretimi")
public class BarcodeController {
    private final BarcodeService barcodeService;

    @PostMapping
    @Operation(summary = "Barkod üret", description = "Gönderilen kategori, birim ve barkod tipi kurallarına göre barkod üretir.")
    public ResponseEntity<BarcodeResponse> generate(@Valid @RequestBody GenerateBarcodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(barcodeService.generate(request));
    }
}