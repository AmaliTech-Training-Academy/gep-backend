package com.event_service.event_service.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentRequestValidator.class)
@Documented
public @interface ValidPaymentRequest {
    String message() default "Invalid payment details for the selected payment method";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
