package com.event_service.event_service.exceptions;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    String emptyRequestMessage = "Invalid request body";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        StringBuilder errorMessageBuilder = new StringBuilder("Validation failed: ");

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMessageBuilder
                    .append(error.getField())
                    .append(" - ")
                    .append(error.getDefaultMessage())
                    .append("; ");
        });

        String errorMessage = errorMessageBuilder.toString().replaceAll(";\\s*$", "");

        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(),
                        errorMessage,
                        HttpStatus.UNPROCESSABLE_ENTITY.value()
                ), HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleEmptyRequestBody(HttpMessageNotReadableException ex, WebRequest request){
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        emptyRequestMessage,
                        HttpStatus.BAD_REQUEST.value()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ResponseEntity<Object> handleResourceNotFound(ResourceNotFound ex, WebRequest request) {
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(),
                        ex.getMessage(),
                        HttpStatus.NOT_FOUND.value()
                ), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<Object> handleAuthorizationAccessDenied(AuthorizationDeniedException ex, WebRequest request) {
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(),
                        ex.getMessage(),
                        HttpStatus.FORBIDDEN.value()
                ), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiErrorResponse> handleCustomValidationException(ValidationException ex) {
        String errorMessage = String.join("; ", ex.getErrors());
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        "Validation failed: " + errorMessage,
                        HttpStatus.UNPROCESSABLE_ENTITY.value()
                ),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(FileUploadException.class)
    public ResponseEntity<ApiErrorResponse> handleFileUploadException(FileUploadException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        ex.getMessage(),
                        HttpStatus.UNPROCESSABLE_ENTITY.value()
                ),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return new ResponseEntity<>(
                new ApiErrorResponse(LocalDateTime.now(),
                        ex.getMessage(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value()
                ), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

