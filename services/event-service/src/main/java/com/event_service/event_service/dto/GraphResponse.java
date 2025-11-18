package com.event_service.event_service.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record GraphResponse (
        List<Object> monthlyData,
        EventGraphMetaDataResponse metadata
) {
}
