package com.event_service.event_service.services;


import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.event_service.event_service.exceptions.ValidationException;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventTypeEnum;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.strategies.*;
import com.event_service.event_service.validations.EventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    @Mock private EventTypeService eventTypeService;
    @Mock private EventMeetingTypeService eventMeetingTypeService;
    @Mock private InPersonAndDayEventStrategy inPersonAndDayEventStrategy;
    @Mock private InPersonAndMultiDayEventStrategy inPersonAndMultiDayEventStrategy;
    @Mock private VirtualAndDayEventStrategy virtualAndDayEventStrategy;
    @Mock private VirtualAndMultiDayEventStrategy virtualAndMultiDayEventStrategy;
    @Mock private EventValidator eventValidator;
    @Mock private EventMapper eventMapper;
    @Mock private EventRepository eventRepository;

    @InjectMocks
    private EventServiceImpl eventService;

    private EventRequest eventRequest;
    private MockMultipartFile flyer;
    private List<MultipartFile> eventImages;

    private EventType eventType;
    private EventMeetingType eventMeetingType;
    private Event event;
    private EventResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        flyer = new MockMultipartFile("flyer", "flyer.jpg", "image/jpeg", "fake".getBytes());
        eventImages = List.of(new MockMultipartFile("img", "img1.jpg", "image/jpeg", "fake".getBytes()));

        eventRequest = mock(EventRequest.class);
        event = Event.builder().id(1L).title("My Event").build();
        response = new EventResponse(1L, "My Event", "desc", null, "Accra", "url", "+0");
    }

    @Test
    void createEvent_inPersonSingleDay_success() {
        eventType = new EventType();
        eventType.setName(EventTypeEnum.DAY_EVENT);

        eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.IN_PERSON);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(inPersonAndDayEventStrategy.createEvent(any(), any(), any(), any(), any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages);

        verify(eventValidator).validateRequiredGroup(eventRequest);
        verify(eventValidator).validateInPersonSingleDayGroup(eventRequest);
        verify(inPersonAndDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void createEvent_inPersonMultiDay_success() {
        eventType = new EventType();
        eventType.setName(EventTypeEnum.MULTI_DAY_EVENT);
        eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.IN_PERSON);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(inPersonAndMultiDayEventStrategy.createEvent(any(), any(), any(), any(), any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages);

        verify(eventValidator).validateRequiredGroup(eventRequest);
        verify(eventValidator).validateInPersonMultiDayGroup(eventRequest);
        verify(inPersonAndMultiDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void createEvent_virtualSingleDay_success() {
        eventType = new EventType();
        eventType.setName(EventTypeEnum.DAY_EVENT);
        eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.VIRTUAL);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(virtualAndDayEventStrategy.createEvent(any(), any(), any(), any(), any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages);

        verify(eventValidator).validateVirtualSingleDayGroup(eventRequest);
        verify(virtualAndDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void createEvent_virtualMultiDay_success() {
        eventType = new EventType();
        eventType.setName(EventTypeEnum.MULTI_DAY_EVENT);
        eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.VIRTUAL);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(virtualAndMultiDayEventStrategy.createEvent(any(), any(), any(), any(), any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages);

        verify(eventValidator).validateVirtualMultiDayGroup(eventRequest);
        verify(virtualAndMultiDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType);
        assertThat(result).isEqualTo(response);
    }

    @Test
    void createEvent_tooManyImages_throwsValidationException() {
        List<MultipartFile> tooMany = List.of(
                flyer, flyer, flyer, flyer, flyer, flyer
        );

        assertThrows(ValidationException.class, () ->
                eventService.createEvent(eventRequest, flyer, tooMany));
    }
}
