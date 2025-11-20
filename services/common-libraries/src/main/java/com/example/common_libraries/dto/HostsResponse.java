package com.example.common_libraries.dto;

import lombok.Builder;

@Builder
public record HostsResponse (
        Long id,
        String fullName,
        String email,
        String role
) { }
