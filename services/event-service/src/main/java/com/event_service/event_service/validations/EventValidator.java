package com.event_service.event_service.validations;

import com.event_service.event_service.dto.EventRequest;
import com.example.common_libraries.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EventValidator {

    private final Validator validator;

    public void validateRequiredGroup(EventRequest eventRequest) {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(eventRequest, RequiredFieldsGroup.class);
        if (!violations.isEmpty()) {
            handleValidationException(violations);
        }
    }

    public void validateInPersonSingleDayGroup(EventRequest eventRequest) {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(eventRequest, InPersonAndSingleDayGroup.class);
        if (!violations.isEmpty()) {
            handleValidationException(violations);
        }
    }

    public void validateInPersonMultiDayGroup(EventRequest eventRequest) {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(eventRequest, InPersonAndMultiDayGroup.class);
        if (!violations.isEmpty()) {
            handleValidationException(violations);
        }
    }

    public void validateVirtualMultiDayGroup(EventRequest eventRequest) {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(eventRequest, VirtualAndMultiDayGroup.class);
        if (!violations.isEmpty()) {
            handleValidationException(violations);
        }
    }

    public void validateVirtualSingleDayGroup(EventRequest eventRequest) {
        Set<ConstraintViolation<EventRequest>> violations = validator.validate(eventRequest, VirtualAndSingleDayGroup.class);
        if (!violations.isEmpty()) {
            handleValidationException(violations);
        }
    }

    private void handleValidationException(Set<ConstraintViolation<EventRequest>> violations) {
        List<String> errors = violations.stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());
        throw new ValidationException(errors);
    }

}
