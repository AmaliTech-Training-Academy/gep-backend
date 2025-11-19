package com.event_service.event_service.dto;

import lombok.Builder;

@Builder
public record EventGraphMetaDataResponse(
        Integer thisYear,
        Integer lastYear,
        Long maxValue,
        String dateRange
) {
}
