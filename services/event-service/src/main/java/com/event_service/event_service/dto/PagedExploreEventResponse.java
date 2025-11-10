package com.event_service.event_service.dto;

import java.util.List;

public record PagedExploreEventResponse(
        Integer pageNumber,
        Integer pageSize,
        List<ExploreEventResponse> events
) {
}
