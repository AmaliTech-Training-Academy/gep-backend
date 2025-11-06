package com.example.common_libraries.exception;

public class InvalidJWTTokenException extends RuntimeException {
    public InvalidJWTTokenException(String message) {
        super(message);
    }
}
