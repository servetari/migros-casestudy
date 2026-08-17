package com.migros.casestudy.barcode.exception;

import com.migros.casestudy.barcode.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request
    ) {
        Map<String, String> errors = exception.getBindingResult().getFieldErrors().stream()
                .collect(java.util.stream.Collectors.toMap(
                        error -> error.getField(), error -> error.getDefaultMessage(),
                        (first, ignored) -> first
                ));
        return response(HttpStatus.BAD_REQUEST, "İstek doğrulanamadı.", request, errors);
    }

    @ExceptionHandler(BarcodeValidationException.class)
    public ResponseEntity<ErrorResponse> handleBarcodeValidation(
            BarcodeValidationException exception, HttpServletRequest request
    ) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BarcodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateBarcode(
            BarcodeAlreadyExistsException exception, HttpServletRequest request
    ) {
        return response(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BarcodeSequenceExhaustedException.class)
    public ResponseEntity<ErrorResponse> handleSequenceExhausted(
            BarcodeSequenceExhaustedException exception, HttpServletRequest request
    ) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BarcodeStrategyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMissingStrategy(
            BarcodeStrategyNotFoundException exception, HttpServletRequest request
    ) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception, HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "İstek gövdesi okunamadı. unit ve type alanları geçerli enum değerleri olmalıdır.",
                request,
                Map.of()
        );
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleUnexpectedBusinessException(
            RuntimeException exception, HttpServletRequest request
    ) {
        return response(HttpStatus.UNPROCESSABLE_ENTITY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(
            Exception exception, HttpServletRequest request
    ) {
        return response(HttpStatus.INTERNAL_SERVER_ERROR, "Beklenmeyen bir hata oluştu.", request, Map.of());
    }

    private ResponseEntity<ErrorResponse> response(
            HttpStatus status, String message, HttpServletRequest request, Map<String, String> errors
    ) {
        return ResponseEntity.status(status).body(new ErrorResponse(
                status.value(), status.getReasonPhrase(), message,
                request.getRequestURI(), LocalDateTime.now(), errors
        ));
    }
}