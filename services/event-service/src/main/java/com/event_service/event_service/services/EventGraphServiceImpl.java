package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventGraphMetaDataResponse;
import com.event_service.event_service.dto.projection.EventMonthlyStatsProjection;
import com.event_service.event_service.dto.GraphResponse;
import com.event_service.event_service.dto.projection.RegistrationMonthlyStatsProjection;
import com.event_service.event_service.repositories.EventRegistrationRepository;
import com.event_service.event_service.repositories.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventGraphServiceImpl implements EventGraphService{
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    @Override
    public GraphResponse getEventGraphData() {
        int currentYear = LocalDate.now().getYear();
        int previousYear = currentYear - 1;
        List<EventMonthlyStatsProjection> monthlyGraphData = eventRepository.getMonthlyEventStats(currentYear,previousYear);

        Long maxValue = monthlyGraphData
                .stream()
                .mapToLong(EventMonthlyStatsProjection::getTotalEventsCreated)
                .max()
                .orElse(0L);

        // Compute date range
        Optional<LocalDate> minDate = monthlyGraphData.stream()
                .map(e -> LocalDate.of(e.getYear(), e.getMonth(), 1))
                .min(Comparator.naturalOrder());

        Optional<LocalDate> maxDate = monthlyGraphData.stream()
                .map(e -> LocalDate.of(e.getYear(), e.getMonth(), 1))
                .max(Comparator.naturalOrder());

        String dateRange = minDate.isPresent() && maxDate.isPresent()
                ? minDate.get().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                + " - "
                + maxDate.get().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                : "";

        EventGraphMetaDataResponse metadata = EventGraphMetaDataResponse
                .builder()
                .thisYear(currentYear)
                .lastYear(previousYear)
                .maxValue(maxValue)
                .dateRange(dateRange)
                .build();

        return GraphResponse
                .builder()
                .monthlyData(Collections.singletonList(monthlyGraphData))
                .metadata(metadata)
                .build();
    }

    @Override
    public GraphResponse getRegistrationGraphData() {
        int currentYear = LocalDate.now().getYear();
        int previousYear = currentYear - 1;
        List<RegistrationMonthlyStatsProjection> monthlyGraphData = eventRegistrationRepository.getMonthlyRegistrationStats(currentYear,previousYear);

        Long maxValue = monthlyGraphData
                .stream()
                .mapToLong(RegistrationMonthlyStatsProjection::getTotalRegistrations)
                .max()
                .orElse(0L);

        // Compute date range
        Optional<LocalDate> minDate = monthlyGraphData.stream()
                .map(e -> LocalDate.of(e.getYear(), e.getMonth(), 1))
                .min(Comparator.naturalOrder());

        Optional<LocalDate> maxDate = monthlyGraphData.stream()
                .map(e -> LocalDate.of(e.getYear(), e.getMonth(), 1))
                .max(Comparator.naturalOrder());

        String dateRange = minDate.isPresent() && maxDate.isPresent()
                ? minDate.get().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                + " - "
                + maxDate.get().format(DateTimeFormatter.ofPattern("MMM yyyy"))
                : "";

        EventGraphMetaDataResponse metadata = EventGraphMetaDataResponse
                .builder()
                .thisYear(currentYear)
                .lastYear(previousYear)
                .maxValue(maxValue)
                .dateRange(dateRange)
                .build();


        return GraphResponse
                .builder()
                .monthlyData(Collections.singletonList(monthlyGraphData))
                .metadata(metadata)
                .build();
    }
}
