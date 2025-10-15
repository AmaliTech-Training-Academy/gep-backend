package com.example.auth_service.dto.response;

import java.time.LocalDateTime;

public record CustomApiResponse<T>(
        String description,
        T data
) {
    public static <T> CustomApiResponse<T> success(T data) {
        return new CustomApiResponse<>("Success", data);
    }

    public static <T> CustomApiResponse<T> success(String message, T data) {
        return new CustomApiResponse<>( message, data);
    }

    public static CustomApiResponse<Void> success(String message) {
        return new CustomApiResponse<>( message, null);
    }


    public static <T> CustomApiResponse<T> error(String message) {
        return new CustomApiResponse<>( message, null);
    }

    public static <T> CustomApiResponse<T> error(String message, T data) {
        return new CustomApiResponse<>( message, data);
    }
}