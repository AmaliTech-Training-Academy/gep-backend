package com.event_service.event_service.services;


import com.event_service.event_service.dto.EventTypeRequest;
import com.event_service.event_service.dto.EventTypeResponse;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventTypeMapper;
import com.event_service.event_service.models.EventType;
import com.event_service.event_service.models.enums.EventTypeEnum;
import com.event_service.event_service.repositories.EventTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class EventTypeServiceImplTest {

    @Mock
    private EventTypeRepository eventTypeRepository;

    @Mock
    private EventTypeMapper eventTypeMapper;

    @InjectMocks
    private EventTypeServiceImpl eventTypeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void save_ShouldSaveEventType() {
        EventTypeRequest request = new EventTypeRequest(EventTypeEnum.DAY_EVENT);
        EventType eventType = EventType.builder().name(EventTypeEnum.DAY_EVENT).build();

        when(eventTypeRepository.save(any(EventType.class))).thenReturn(eventType);

        eventTypeService.save(request);

        verify(eventTypeRepository, times(1)).save(any(EventType.class));
    }

    @Test
    void findById_ShouldReturnEventType_WhenFound() {
        EventType eventType = EventType.builder().id(1L).name(EventTypeEnum.DAY_EVENT).build();
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.of(eventType));

        EventType found = eventTypeService.findById(1L);
        assertNotNull(found);
        assertEquals(EventTypeEnum.DAY_EVENT, found.getName());
        verify(eventTypeRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldThrowResourceNotFound_WhenNotFound() {
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> eventTypeService.findById(1L));
        verify(eventTypeRepository, times(1)).findById(1L);
    }


    @Test
    void update_ShouldUpdateEventType_WhenFound() {
        EventType eventType = EventType.builder().id(1L).name(EventTypeEnum.DAY_EVENT).build();
        EventTypeRequest request = new EventTypeRequest(EventTypeEnum.MULTI_DAY_EVENT);

        when(eventTypeRepository.findById(1L)).thenReturn(Optional.of(eventType));
        when(eventTypeRepository.save(any(EventType.class))).thenReturn(eventType);

        eventTypeService.update(1L, request);

        verify(eventTypeRepository, times(1)).save(eventType);
        assertEquals(EventTypeEnum.MULTI_DAY_EVENT, eventType.getName());
    }

    @Test
    void update_ShouldThrowResourceNotFound_WhenNotFound() {
        when(eventTypeRepository.findById(1L)).thenReturn(Optional.empty());
        EventTypeRequest request = new EventTypeRequest(EventTypeEnum.MULTI_DAY_EVENT);

        assertThrows(ResourceNotFoundException.class, () -> eventTypeService.update(1L, request));
    }


    @Test
    void findAll_ShouldReturnMappedResponses() {
        EventType type1 = EventType.builder().id(1L).name(EventTypeEnum.DAY_EVENT).build();
        EventType type2 = EventType.builder().id(2L).name(EventTypeEnum.MULTI_DAY_EVENT).build();
        List<EventType> types = Arrays.asList(type1, type2);

        EventTypeResponse res1 = new EventTypeResponse(1L, EventTypeEnum.DAY_EVENT);
        EventTypeResponse res2 = new EventTypeResponse(2L, EventTypeEnum.MULTI_DAY_EVENT);

        when(eventTypeRepository.findAll()).thenReturn(types);
        when(eventTypeMapper.toEventTypeResponse(type1)).thenReturn(res1);
        when(eventTypeMapper.toEventTypeResponse(type2)).thenReturn(res2);


        List<EventTypeResponse> result = eventTypeService.findAll();

        assertEquals(2, result.size());
        assertEquals(EventTypeEnum.DAY_EVENT, result.getFirst().name());
        verify(eventTypeRepository, times(1)).findAll();
        verify(eventTypeMapper, times(2)).toEventTypeResponse(any(EventType.class));
    }
}
