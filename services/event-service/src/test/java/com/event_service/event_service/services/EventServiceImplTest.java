package com.event_service.event_service.services;

import com.event_service.event_service.dto.EventRequest;
import com.event_service.event_service.dto.EventResponse;
import com.example.common_libraries.dto.AppUser;
import com.example.common_libraries.exception.ValidationException;
import com.event_service.event_service.mappers.EventMapper;
import com.event_service.event_service.models.*;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventTypeEnum;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.strategies.*;
import com.event_service.event_service.validations.EventValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
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
    private List<MultipartFile> sectionImages;
    private Event event;
    private EventResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        AppUser currentUser = new AppUser(
                999L,
        "ORGANIZER",
        "testuser@gep.com",
        "Kwame Nkrumah"
        );


        var authentication = new UsernamePasswordAuthenticationToken(
                currentUser,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.setContext(new SecurityContextImpl(authentication));


        flyer = new MockMultipartFile("flyer", "flyer.jpg", "image/jpeg", "fake-flyer".getBytes());
        sectionImages = List.of(
                new MockMultipartFile("images", "img1.jpg", "image/jpeg", "fake-img1".getBytes()),
                new MockMultipartFile("images", "img2.jpg", "image/jpeg", "fake-img2".getBytes())
        );

        eventImages = List.of(
                new MockMultipartFile("images", "img1.jpg", "image/jpeg", "fake-img1".getBytes()),
                new MockMultipartFile("images", "img2.jpg", "image/jpeg", "fake-img2".getBytes())
        );

        eventRequest = mock(EventRequest.class);
        event = Event.builder()
                .id(1L)
                .title("My Amazing Event")
                .build();

        response = new EventResponse(
                1L,
                "My Amazing Event",
                "Join us for an unforgettable experience in Accra!",
                Instant.now(),
                "Accra International Conference Centre",
                "https://storage.gep.com/flyers/event-1.jpg",
                "+00:00",
                "Kwame Nkrumah"
        );
    }

    @Test
    @Disabled("Skipping all EventServiceImpl tests temporarily")
    void createEvent_inPersonSingleDay_success() {
        var eventType = new EventType();
        eventType.setName(EventTypeEnum.DAY_EVENT);
        var eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.IN_PERSON);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(inPersonAndDayEventStrategy.createEvent(any(), any(), any(), any(), any(), any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages,sectionImages);

        verify(eventValidator).validateRequiredGroup(eventRequest);
        verify(eventValidator).validateInPersonSingleDayGroup(eventRequest);
        verify(inPersonAndDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType,sectionImages);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @Disabled("Skipping all EventServiceImpl tests temporarily")
    void createEvent_inPersonMultiDay_success() {
        var eventType = new EventType();
        eventType.setName(EventTypeEnum.MULTI_DAY_EVENT);
        var eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.IN_PERSON);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(inPersonAndMultiDayEventStrategy.createEvent(any(), any(), any(), any(), any(),any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages,sectionImages);

        verify(eventValidator).validateRequiredGroup(eventRequest);
        verify(eventValidator).validateInPersonMultiDayGroup(eventRequest);
        verify(inPersonAndMultiDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType,sectionImages);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @Disabled("Skipping all EventServiceImpl tests temporarily")
    void createEvent_virtualSingleDay_success() {
        var eventType = new EventType();
        eventType.setName(EventTypeEnum.DAY_EVENT);
        var eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.VIRTUAL);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(virtualAndDayEventStrategy.createEvent(any(), any(), any(), any(), any(),any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages,sectionImages);

        verify(eventValidator).validateRequiredGroup(eventRequest);
        verify(eventValidator).validateVirtualSingleDayGroup(eventRequest);
        verify(virtualAndDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType,sectionImages);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @Disabled("Skipping all EventServiceImpl tests temporarily")
    void createEvent_virtualMultiDay_success() {
        var eventType = new EventType();
        eventType.setName(EventTypeEnum.MULTI_DAY_EVENT);
        var eventMeetingType = new EventMeetingType();
        eventMeetingType.setName(EventMeetingTypeEnum.VIRTUAL);

        when(eventTypeService.findById(any())).thenReturn(eventType);
        when(eventMeetingTypeService.findEventMeetingTypeById(any())).thenReturn(eventMeetingType);
        when(virtualAndMultiDayEventStrategy.createEvent(any(), any(), any(), any(), any(),any())).thenReturn(event);
        when(eventMapper.toResponse(event)).thenReturn(response);

        EventResponse result = eventService.createEvent(eventRequest, flyer, eventImages,sectionImages);

        verify(eventValidator).validateRequiredGroup(eventRequest);
        verify(eventValidator).validateVirtualMultiDayGroup(eventRequest);
        verify(virtualAndMultiDayEventStrategy).createEvent(eventRequest, flyer, eventImages, eventType, eventMeetingType,sectionImages);
        assertThat(result).isEqualTo(response);
    }

    @Test
    @Disabled("Skipping all EventServiceImpl tests temporarily")
    void createEvent_tooManyImages_throwsValidationException() {
        List<MultipartFile> tooManyImages = List.of(flyer, flyer, flyer, flyer, flyer, flyer, flyer);

        assertThrows(ValidationException.class, () ->
                eventService.createEvent(eventRequest, flyer, tooManyImages,sectionImages));
    }
}