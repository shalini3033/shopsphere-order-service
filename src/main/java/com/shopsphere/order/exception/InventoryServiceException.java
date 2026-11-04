package com.shopsphere.order.exception;

import org.springframework.http.HttpStatusCode;

public class InventoryServiceException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public InventoryServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}