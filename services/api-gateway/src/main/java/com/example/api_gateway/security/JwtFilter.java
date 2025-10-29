package com.example.api_gateway.security;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
@Component
@Order(-1)
public class JwtFilter implements GlobalFilter {

    private final JwtUtil jwtUtil;

    public static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/api/v1/auth/register",
            "/api/v1/auth/login",
            "/api/v1/auth/verify-otp",
            "/api/v1/auth/refresh",
            "/api/v1/auth/logout",
            "/auth-service/v3/api-docs"
    );


    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path =request.getURI().getPath();

        if(OPEN_API_ENDPOINTS.contains(path)){
            return chain.filter(exchange);
        }

        String token = extractTokenFromCookie(request);

        if (!jwtUtil.validateToken(token)) {
            return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.extractUsername(token);

        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-User-Email", username)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    private String extractTokenFromCookie(ServerHttpRequest request){
        HttpCookie cookie = request.getCookies().getFirst("accessToken");
        return cookie != null ? cookie.getValue() : null;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        return exchange.getResponse().setComplete();
    }
}
