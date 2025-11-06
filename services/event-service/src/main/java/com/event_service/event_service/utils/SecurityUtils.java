package com.event_service.event_service.utils;

import com.example.common_libraries.exception.UnauthorizedException;
import com.event_service.event_service.models.AppUser;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {


    public AppUser getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationDeniedException("User is not authenticated");
        }

        if(authentication.getPrincipal() instanceof AppUser){
            return (AppUser) authentication.getPrincipal();
        } else {
            throw new AuthorizationDeniedException("User is not authenticated");
        }
    }
}
