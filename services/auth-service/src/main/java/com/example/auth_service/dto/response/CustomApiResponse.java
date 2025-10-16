package com.example.auth_service.dto.response;

public record CustomApiResponse<T>(
        String description,
        T data
) {

    public static <T> CustomApiResponse<T> error(String message) {
        return new CustomApiResponse<>( message, null);
    }

    public static <T> CustomApiResponse<T> error(String message, T data) {
        return new CustomApiResponse<>( message, data);
    }
}