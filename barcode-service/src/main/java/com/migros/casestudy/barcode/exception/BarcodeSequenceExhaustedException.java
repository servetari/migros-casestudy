package com.migros.casestudy.barcode.exception;

import com.migros.casestudy.barcode.entity.enums.BarcodeType;

public class BarcodeSequenceExhaustedException extends RuntimeException {
    public BarcodeSequenceExhaustedException(BarcodeType type, int maxSequence) {
        super(type + " barkodu için sequence limiti doldu. Maksimum: " + maxSequence);
    }
}