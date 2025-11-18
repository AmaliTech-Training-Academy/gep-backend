package com.example.common_libraries.exception.handlers;

import com.example.common_libraries.dto.CustomApiResponse;
import com.example.common_libraries.exception.*;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.reactive.resource.NoResourceFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<CustomApiResponse<?>> handleValidationException(ValidationException ex){
        String errorMessage = String.join("; ", ex.getErrors());
        return new ResponseEntity<>(
                CustomApiResponse.error(
                        "Validation failed: ",
                        errorMessage
                ),
                HttpStatus.UNPROCESSABLE_ENTITY
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<CustomApiResponse<?>> handleBadRequestException(BadRequestException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<CustomApiResponse<?>> handleDuplicateResourceException(DuplicateResourceException ex){
        return ResponseEntity.status(HttpStatus.CONFLICT).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<CustomApiResponse<?>> handleResourceNotFoundException(ResourceNotFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InvalidJWTTokenException.class)
    public ResponseEntity<CustomApiResponse<?>> handleExpiredJwtException(InvalidJWTTokenException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InactiveAccountException.class)
    public ResponseEntity<CustomApiResponse<?>> handleInactiveAccountException(InactiveAccountException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<CustomApiResponse<?>> handleUnauthorizedException(UnauthorizedException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CustomApiResponse<?>> handleBadCredentialsException(BadCredentialsException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(InputOutputException.class)
    public ResponseEntity<CustomApiResponse<?>> handleInputOutputException(InputOutputException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ServiceCommunicationException.class)
    public ResponseEntity<CustomApiResponse<?>> handleServiceCommunicationException(ServiceCommunicationException ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<CustomApiResponse<?>> handleForbiddenException(ForbiddenException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CustomApiResponse.error(ex.getMessage()));
    }

    // Core Exceptions
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<CustomApiResponse<?>> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex){
        if(ex.getRequiredType() == LocalDate.class){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomApiResponse.error("Invalid date format. Use yyyy-MM-dd"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomApiResponse.error("Invalid parameter type"));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<CustomApiResponse<?>> handleExpiredJwtException(ExpiredJwtException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<CustomApiResponse<?>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, Object> body = new HashMap<>();

        // Collect field -> message pairs
        body.put("violations", ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        v -> v.getMessage()
                )));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error("Validation failed", body));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CustomApiResponse<ArrayList<String>>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        ArrayList<String> errors = new ArrayList<>();
        ex.getBindingResult().getGlobalErrors().forEach(error -> errors.add(error.getDefaultMessage()));
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.add(error.getDefaultMessage()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CustomApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<CustomApiResponse<?>> handleUnauthorizedAccess(DisabledException ex){
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<CustomApiResponse<?>> handleMissingServletException(MissingServletRequestPartException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<CustomApiResponse<?>> handleAuthorizationDeniedException(AuthorizationDeniedException ex){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<CustomApiResponse<?>> handleEmptyBody(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomApiResponse.error("Request body is missing or malformed."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<CustomApiResponse<?>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex){
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<CustomApiResponse<?>> handleMissingRequestCookie(MissingRequestCookieException ex){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<CustomApiResponse<?>> handleNoResourceFoundException(NoResourceFoundException ex){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(CustomApiResponse.error(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CustomApiResponse<Object>> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return new ResponseEntity<>(
                CustomApiResponse.error(ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    // General Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CustomApiResponse<?>> handleGenericException(Exception ex){
        return ResponseEntity.internalServerError().body(CustomApiResponse.error("Unexpected error occured"));
    }
}
