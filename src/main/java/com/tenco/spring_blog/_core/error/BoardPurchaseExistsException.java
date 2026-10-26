package com.tenco.spring_blog._core.error;

public class BoardPurchaseExistsException extends RuntimeException {
    public BoardPurchaseExistsException(String message) {
        super(message);
    }
}
