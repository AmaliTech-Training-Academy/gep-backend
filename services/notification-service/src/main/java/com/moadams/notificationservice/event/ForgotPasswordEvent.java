package com.moadams.notificationservice.event;

public record ForgotPasswordEvent(String email,
                                  String fullName,
                                  String otp) {
}
