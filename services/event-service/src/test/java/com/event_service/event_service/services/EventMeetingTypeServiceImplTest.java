package com.event_service.event_service.services;


import com.event_service.event_service.dto.EventMeetingTypeRequest;
import com.event_service.event_service.dto.EventMeetingTypeResponse;
import com.example.common_libraries.exception.ResourceNotFoundException;
import com.event_service.event_service.mappers.EventMeetingMapper;
import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.repositories.EventMeetingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventMeetingTypeServiceImplTest {

    @Mock
    private EventMeetingTypeRepository repository;

    @Mock
    private EventMeetingMapper mapper;

    @InjectMocks
    private EventMeetingTypeServiceImpl service;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEventMeetingType_shouldSaveEntity() {
        EventMeetingTypeRequest request = new EventMeetingTypeRequest(EventMeetingTypeEnum.IN_PERSON);
        EventMeetingType entity = EventMeetingType.builder().name(EventMeetingTypeEnum.IN_PERSON).build();

        when(repository.save(any(EventMeetingType.class))).thenReturn(entity);

        service.createEventMeetingType(request);

        verify(repository, times(1)).save(any(EventMeetingType.class));
    }

    @Test
    void findEventMeetingTypeById_shouldReturnEntity() {
        EventMeetingType entity = EventMeetingType.builder().id(1L).name(EventMeetingTypeEnum.IN_PERSON).build();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        EventMeetingType result = service.findEventMeetingTypeById(1L);

        assertThat(result.getName()).isEqualTo(EventMeetingTypeEnum.IN_PERSON);
    }

    @Test
    void findEventMeetingTypeById_shouldThrowWhenNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findEventMeetingTypeById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findAllEventMeetingTypes_shouldMapToResponse() {
        EventMeetingType entity = EventMeetingType.builder().name(EventMeetingTypeEnum.IN_PERSON).build();
        EventMeetingTypeResponse response = new EventMeetingTypeResponse(1L, EventMeetingTypeEnum.IN_PERSON);

        when(repository.findAll()).thenReturn(List.of(entity));
        when(mapper.toEventMeetingTypeResponse(entity)).thenReturn(response);

        List<EventMeetingTypeResponse> result = service.findAllEventMeetingTypes();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo(EventMeetingTypeEnum.IN_PERSON);
    }

    @Test
    void updateEventMeetingType_shouldUpdateEntity() {
        EventMeetingType existing = EventMeetingType.builder().id(1L).name(EventMeetingTypeEnum.VIRTUAL).build();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        EventMeetingTypeRequest request = new EventMeetingTypeRequest(EventMeetingTypeEnum.IN_PERSON);
        service.updateEventMeetingType(1L, request);

        assertThat(existing.getName()).isEqualTo(EventMeetingTypeEnum.IN_PERSON);
        verify(repository).save(existing);
    }
}
