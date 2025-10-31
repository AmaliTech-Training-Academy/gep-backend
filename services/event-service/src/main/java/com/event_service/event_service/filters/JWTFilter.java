package com.event_service.event_service.filters;


import com.event_service.event_service.exceptions.InvalidJWTTokenException;
import com.event_service.event_service.models.AppUser;
import com.event_service.event_service.utilities.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JWTFilter extends OncePerRequestFilter {
    private static final String ACCESS_TOKEN_COOKIE = "accessToken";

    private final JWTUtil jwtUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws IOException {
        try {
            String token = null;
            if (request.getCookies() != null) {
                for (Cookie cookie : request.getCookies()) {
                    if (ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                        token = cookie.getValue();
                        break;
                    }
                }
            }
            if (token == null || token.isEmpty()) {
                filterChain.doFilter(request, response);
                return;
            }
            jwtUtil.validateToken(token);
            Claims userDetails = jwtUtil.parseToken(token);
            String userEmail = userDetails.getSubject();
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);


            AppUser appUser = new AppUser(userId,role, userEmail);

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    appUser, null, appUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(e.getMessage());
        }
    }
}
