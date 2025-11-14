package com.event_service.event_service.dto;

import jakarta.validation.constraints.*;

public record EventRegistrationRequest(

        @NotNull(message = "Ticket type ID is required.")
        @Positive(message = "Ticket type ID must be a positive number.")
        Long ticketTypeId,

        @NotNull(message = "Number of tickets is required.")
        @Min(value = 1, message = "You must request at least one ticket.")
        @Max(value = 10, message = "You cannot request more than 10 tickets at once.")
        Long numberOfTickets,

        @NotBlank(message = "Full name is required.")
        @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters.")
        @Pattern(
                regexp = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\- ]+$",
                message = "Full name can only contain letters, spaces, hyphens, and apostrophes."
        )
        String fullName,

        @NotBlank(message = "Email is required.")
        @Email(message = "Please provide a valid email address.")
        @Size(max = 150, message = "Email cannot exceed 150 characters.")
        String email
) { }