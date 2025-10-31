package com.event_service.event_service.exceptions;

public class DuplicateInvitationException extends RuntimeException {
    public DuplicateInvitationException(String message) {
        super(message);
    }
}
