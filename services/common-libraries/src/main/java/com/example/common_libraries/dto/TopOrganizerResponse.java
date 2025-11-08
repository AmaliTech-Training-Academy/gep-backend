package com.example.common_libraries.dto;

import lombok.Builder;

@Builder
public record TopOrganizerResponse(
        String name,
        String email,
        Long eventCount,
        double growthPercentage
) {
}
