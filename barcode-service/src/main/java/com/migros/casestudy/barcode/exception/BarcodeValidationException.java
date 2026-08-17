package com.migros.casestudy.barcode.exception;

public class BarcodeValidationException extends IllegalArgumentException {
    public BarcodeValidationException(String message) {
        super(message);
    }

    public BarcodeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}