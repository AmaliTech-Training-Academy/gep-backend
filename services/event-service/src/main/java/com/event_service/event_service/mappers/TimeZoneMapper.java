package com.event_service.event_service.mappers;


import com.event_service.event_service.dto.TimeZoneResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface TimeZoneMapper {
    @Mapping(target = "zoneId", source = "zoneId")
    @Mapping(target = "gmtOffset", source = "zoneId", qualifiedByName = "toGmtOffset")
    @Mapping(target = "displayName", source = "zoneId", qualifiedByName = "toDisplayName")
    TimeZoneResponse toResponse(String zoneId);

    default List<TimeZoneResponse> toResponseList(Set<String> zoneIds) {
        return zoneIds.stream()
                .sorted()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Named("toGmtOffset")
    default String toGmtOffset(String zoneId) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of(zoneId));
        String offset = now.getOffset().getId().replace("Z", "+00:00");
        return "GMT" + offset;
    }

    @Named("toDisplayName")
    default String toDisplayName(String zoneId) {
        return ZoneId.of(zoneId).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

}
