package com.example.auth_service.utils;

import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Objects;

/**
 * The ResourceOwnership class provides functionality to verify if a specific user is
 * the owner of a resource in the system. The ownership is checked by comparing
 * the user ID associated with the resource to the ID of the currently authenticated user.
 *
 * The class relies on the UserRepository to retrieve user information based on email.
 * It uses a Principal object to determine the authenticated user's email.
 *
 * It is annotated with {@code @Component} for Spring to identify it as a Spring-managed
 * bean and {@code @RequiredArgsConstructor} to generate a constructor with required
 * dependencies.
 */
@Component("resourceOwner")
@RequiredArgsConstructor
public class ResourceOwnership {
    private final UserRepository userRepository;

    public boolean isOwner(Long objectUserId , Principal principal) {
        String userEmail = principal.getName();
        return userRepository.findByEmail(userEmail)
                .map(user -> Objects.equals(user.getId(), objectUserId))
                .orElse(false);
    }
}