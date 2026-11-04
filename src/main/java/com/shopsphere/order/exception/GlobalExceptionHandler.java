package com.shopsphere.order.exception;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductServiceException.class)
    public ResponseEntity<Map<String, String>> handleProductServiceException(
            ProductServiceException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(InventoryServiceException.class)
    public ResponseEntity<Map<String, String>> handleInventoryServiceException(
            InventoryServiceException ex) {

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("error", ex.getMessage()));
    }
}