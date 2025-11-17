package com.example.auth_service.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlatformSecuritySettingRequest(
        @Size(max = 255, message = "Platform name must be at most 255 characters")
        String platformName,
        @Size(max = 255, message = "Platform URL must be at most 255 characters")
        @Pattern(
                regexp = "^(https?://).+$",
                message = "Platform URL must start with http:// or https://"
        )
    String platformUrl,
        @Email(message = "Contact email must be valid")
        @Size(max = 255, message = "Contact email must be at most 255 characters")
    String contactEmail,
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String platformDescription,
    Boolean maintenanceMode
) {
}
