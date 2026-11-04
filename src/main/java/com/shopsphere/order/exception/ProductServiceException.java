package com.shopsphere.order.exception;

import org.springframework.http.HttpStatusCode;

public class ProductServiceException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public ProductServiceException(String message, HttpStatusCode statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}