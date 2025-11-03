package com.event_service.event_service.validations;

import com.event_service.event_service.dto.PaymentRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentRequestValidator implements ConstraintValidator<ValidPaymentRequest, PaymentRequest> {
    @Override
    public boolean isValid(PaymentRequest paymentRequest, ConstraintValidatorContext constraintValidatorContext) {
        if(paymentRequest == null) return true; // Return true if free event

        switch(paymentRequest.paymentMethod()){
            case MOBILE_BANKING -> {
                return paymentRequest.mobileNumber() != null && !paymentRequest.mobileNumber().isBlank()
                        && paymentRequest.provider() != null && !paymentRequest.provider().name().isBlank();
            }
            case CARD -> {
                return paymentRequest.cardNumber() != null && !paymentRequest.cardNumber().isBlank()
                        && paymentRequest.cardHolderName() != null && !paymentRequest.cardHolderName().isBlank()
                        && paymentRequest.expiryDate() != null && !paymentRequest.expiryDate().isBlank()
                        && paymentRequest.cvv() != null && !paymentRequest.cvv().isBlank();
            }
            default -> {
                return false;
            }
        }
    }
}
