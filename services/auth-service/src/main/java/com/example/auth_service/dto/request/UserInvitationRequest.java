package com.example.auth_service.dto.request;

import com.example.auth_service.enums.UserRole;
import jakarta.validation.constraints.*;

public record UserInvitationRequest(@NotBlank(message = "Fullname is required")
                                    @Pattern(
                                            regexp = "^[\\p{L}]+([ '-][\\p{L}]+)*$",
                                            message = "Full name must start and end with a letter, and contain only letters, spaces, hyphens, or apostrophes"
                                    )
                                    String fullName,
                                    @NotBlank(message = "Email is required")
                                    @Email(message = "Invalid email format")
                                    String email,
                                    @NotNull(message = "Role is required")
                                    UserRole role
){
}
