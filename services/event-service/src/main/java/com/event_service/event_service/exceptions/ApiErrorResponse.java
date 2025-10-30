package com.event_service.event_service.exceptions;

import java.time.LocalDateTime;

public record ApiErrorResponse(
        LocalDateTime timestamp,
        String errorMessage,
        int statusCode
) {
}
