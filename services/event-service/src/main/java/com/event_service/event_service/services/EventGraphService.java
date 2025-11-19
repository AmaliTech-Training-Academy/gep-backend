package com.event_service.event_service.services;

import com.event_service.event_service.dto.GraphResponse;

public interface EventGraphService {
    GraphResponse getEventGraphData();
    GraphResponse getRegistrationGraphData();
}
