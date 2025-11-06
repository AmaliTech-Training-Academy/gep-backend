package com.example.common_libraries.dto;


import com.example.common_libraries.enums.MomoProvider;
import com.example.common_libraries.enums.PaymentMethod;
import com.example.common_libraries.validators.ValidPaymentRequest;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

@ValidPaymentRequest
public record PaymentRequest(
       @NotNull(message = "Payment method is required.")
       PaymentMethod paymentMethod,

       // General fields MOMO and CARD details
       @NotNull(message = "Amount is required.")
       @Positive(message = "Amount must be greater than zero.")
       Double amount,

       // MOMO specific fields
       @Pattern(
               regexp = "^(\\+\\d{1,3}|0)[0-9]{6,12}$",
               message = "Invalid mobile money phone number format."
       )
       String mobileNumber,

       @Nullable
       MomoProvider provider,

       // CARD-specific fields
       @Pattern(regexp = "^[0-9]{16}$", message = "Card number must be 16 digits.")
       String cardNumber,

       @Pattern(regexp = "^(0[1-9]|1[0-2])\\/([0-9]{2})$", message = "Expiry date must be in MM/YY format.")
       String expiryDate,

       @Pattern(regexp = "^[0-9]{3,4}$", message = "Invalid CVV.")
       String cvv,

       @Size(max = 100, message = "Cardholder name too long.")
       String cardHolderName
) { }
