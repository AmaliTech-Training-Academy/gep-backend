package com.example.auth_service.utils;

import com.example.auth_service.model.User;
import com.example.auth_service.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUserUtil {

    public User getAuthenticatedUser(){
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        AuthUser authUser = (AuthUser) authentication.getPrincipal();
        return authUser.getUser();
    }
}
