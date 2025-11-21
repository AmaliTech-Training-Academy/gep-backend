package com.event_service.event_service.dto;

import com.event_service.event_service.validations.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record EventRequest(
        @NotBlank(message = "Title is required", groups = {RequiredFieldsGroup.class})
        String title,

        @NotBlank(message = "Description is required",
                groups = {RequiredFieldsGroup.class})
        String description,

        @NotNull(message = "Event type ID is required",
                groups = {RequiredFieldsGroup.class})
        @Positive(message = "Event type ID must be a positive number",
                groups = {RequiredFieldsGroup.class})
        Long event_type_id,

        @NotNull(message = "Event meeting type ID is required",
                groups = {RequiredFieldsGroup.class})
        @Positive(message = "Event meeting type ID must be a positive number",
                groups = {RequiredFieldsGroup.class})
        Long event_meeting_type_id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        @NotNull(message = "Event date is required", groups = {
                InPersonAndSingleDayGroup.class,
                VirtualAndSingleDayGroup.class,
        })
        @FutureOrPresent(message = "Event end date must be in the present or future")
        LocalDate event_date,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull(message = "Event time is required", groups = {
                InPersonAndSingleDayGroup.class,
                VirtualAndSingleDayGroup.class,
        })
        LocalTime event_time,

        @NotBlank(message = "Event location is required",
                groups = {
                InPersonAndSingleDayGroup.class, InPersonAndMultiDayGroup.class
        })
        String location,

        @NotBlank(message = "Zoom link is required",
                groups = {VirtualAndSingleDayGroup.class,
                        VirtualAndMultiDayGroup.class
        })
        String zoomUrl,
        @NotBlank(message = "Event time zone is required",
                groups = {InPersonAndSingleDayGroup.class,
                        VirtualAndSingleDayGroup.class,
        })
        String event_time_zone_id,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        @NotNull(message = "Event start date is required",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        @FutureOrPresent(message = "Event start date must be in the present or future",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        LocalDate event_start_time_date,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
        @NotNull(message = "Event end date is required",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        @FutureOrPresent(message = "Event end date must be in the present or future",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        LocalDate event_end_time_date,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull(message = "Event start time is required",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        LocalTime event_start_time,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
        @NotNull(message = "Event end time is required",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        LocalTime event_end_time,

        @NotBlank(message = "Event start time zone is required",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        String event_start_time_zone_id,
        @NotBlank(message = "Event end time zone is required",
                groups = {InPersonAndMultiDayGroup.class,
                        VirtualAndMultiDayGroup.class
                })
        String event_end_time_zone_id,

        @Valid
        @NotNull(message = "Event options are required")
        EventOptionsRequest eventOptionsRequest,

        List<EventSectionRequest> eventSectionRequest
) {}

