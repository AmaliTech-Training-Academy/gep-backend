package com.event_service.event_service.utilities;

import com.event_service.event_service.exceptions.UnauthorizedException;
import com.event_service.event_service.models.AppUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {


    public AppUser getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated");
        }

        if(authentication.getPrincipal() instanceof AppUser){
            return (AppUser) authentication.getPrincipal();
        } else {
            throw new UnauthorizedException("User is not authenticated");
        }
    }
}
