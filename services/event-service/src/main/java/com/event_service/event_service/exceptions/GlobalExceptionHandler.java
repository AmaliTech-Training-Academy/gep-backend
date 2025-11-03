package com.event_service.event_service.exceptions;


import com.event_service.event_service.dto.CustomApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestControllerAdvice
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
    public ResponseEntity<CustomApiResponse<Object>> handleEmptyRequestBody(HttpMessageNotReadableException ex, WebRequest request){
        return new ResponseEntity<>(
                CustomApiResponse.error(emptyRequestMessage),
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

    @ExceptionHandler(InvalidInvitationException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleInvalidInvitation(InvalidInvitationException ex, WebRequest request) {
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleUserAlreadyExistException(UserAlreadyExistsException ex, WebRequest request) {
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleAuthorizationAccessDenied(AuthorizationDeniedException ex, WebRequest request) {
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleCustomValidationException(ValidationException ex) {
        String errorMessage = String.join("; ", ex.getErrors());
        return new ResponseEntity<>(
                CustomApiResponse.error(
                        "Validation failed: ",
                        errorMessage
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

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequestException(BadRequestException ex) {
        return new ResponseEntity<>(
                new ApiErrorResponse(
                        LocalDateTime.now(),
                        ex.getMessage(),
                        HttpStatus.BAD_REQUEST.value()
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DuplicateInvitationException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleDuplicateInvitationException(
            DuplicateInvitationException ex,
            WebRequest request) {

        log.warn("Duplicate invitation attempt: {}", ex.getMessage());

        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleEventNotFoundException(EventNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleNoResourceFoundException(NoResourceFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvitationPublishException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleInvitationPublishException(
            InvitationPublishException ex,
            WebRequest request) {
        log.error("Failed to publish invitation email event to SQS", ex);
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
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

