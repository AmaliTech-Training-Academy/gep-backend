package com.event_service.event_service.utils;

import com.example.common_libraries.dto.AppUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUtils {


    public AppUser getCurrentUser(){
        log.info("Getting current user");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthorizationDeniedException("User is not authenticated");
        }
        Object principal = authentication.getPrincipal();
        log.info("Current user class: {}", principal.getClass().getName());
        log.info("Current user: {}", principal);


        if(authentication.getPrincipal() instanceof AppUser){
            log.info("User is an instance of AppUser");
            return (AppUser) authentication.getPrincipal();
        } else {
            log.error("User is not an instance of AppUser. Actual type: {}", principal.getClass().getName());

            throw new AuthorizationDeniedException("User is not authenticated");
        }
    }
}
