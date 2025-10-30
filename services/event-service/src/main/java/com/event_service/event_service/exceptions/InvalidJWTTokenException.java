package com.event_service.event_service.exceptions;

public class InvalidJWTTokenException extends RuntimeException {
    public InvalidJWTTokenException(String message) {
        super(message);
    }
}
