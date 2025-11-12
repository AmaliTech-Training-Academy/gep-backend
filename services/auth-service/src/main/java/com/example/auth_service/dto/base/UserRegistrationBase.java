package com.example.auth_service.dto.base;

import com.example.auth_service.enums.UserRole;

public interface UserRegistrationBase {
    String fullName();
    String email();
    String password();
    String confirmPassword();
    UserRole role();
}
