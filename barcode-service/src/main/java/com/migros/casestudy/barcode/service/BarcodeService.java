package com.migros.casestudy.barcode.service;

import com.migros.casestudy.barcode.dto.request.GenerateBarcodeRequest;
import com.migros.casestudy.barcode.dto.response.BarcodeResponse;

public interface BarcodeService {
    BarcodeResponse generate(GenerateBarcodeRequest request);
}