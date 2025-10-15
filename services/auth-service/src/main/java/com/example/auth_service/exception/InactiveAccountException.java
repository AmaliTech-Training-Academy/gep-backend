package com.example.auth_service.exception;

public class InactiveAccountException extends RuntimeException {
    public InactiveAccountException(String message) {
        super(message);
    }
}
