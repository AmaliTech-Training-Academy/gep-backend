//package com.event_service.event_service;
//
//import com.event_service.event_service.dto.EventInvitationRequest;
//import com.event_service.event_service.event.EventInvitationEvent;
//import com.event_service.event_service.exceptions.InvitationPublishException;
//import com.event_service.event_service.models.AppUser;
//import com.event_service.event_service.models.Event;
//import com.event_service.event_service.models.EventInvitation;
//import com.event_service.event_service.models.enums.InviteeRole;
//import com.event_service.event_service.repositories.EventInvitationRepository;
//import com.event_service.event_service.repositories.EventRepository;
//import com.event_service.event_service.services.EventInvitationServiceImpl;
//import com.event_service.event_service.utilities.SecurityUtils;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.test.util.ReflectionTestUtils;
//import software.amazon.awssdk.services.sqs.SqsClient;
//import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
//
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class EventInvitationServiceImplTest {
//
//    @Mock
//    private SecurityUtils securityUtils;
//
//    @Mock
//    private ObjectMapper objectMapper;
//
//    @Mock
//    private SqsClient sqsClient;
//
//    @Mock
//    private EventRepository eventRepository;
//
//    @Mock
//    private EventInvitationRepository eventInvitationRepository;
//
//    @InjectMocks
//    private EventInvitationServiceImpl invitationService;
//
//    private static final String FRONTEND_BASE_URL = "https://example.com";
//    private static final String QUEUE_URL = "https://sqs.us-east-1.amazonaws.com/123456789/invitation-queue";
//
//    private AppUser currentUser;
//    private Event event;
//    private EventInvitationRequest request;
//
//    @BeforeEach
//    void setUp() {
//        ReflectionTestUtils.setField(invitationService, "frontendBaseUrl", FRONTEND_BASE_URL);
//        ReflectionTestUtils.setField(invitationService, "invitationQueueUrl", QUEUE_URL);
//
//        currentUser = new AppUser(1L, "USER", "organizer@example.com");
//
//        event = Event.builder()
//                .id(1L)
////                .userId(1L)
//                .title("Test Event")
//                .description("Test Description")
//                .flyerUrl("http://example.com/flyer.jpg")
//                .build();
//
//        request = new EventInvitationRequest(
//                "NSMQ 2025",
//                "Killer Ntua",
//                "killer@example.com",
//                InviteeRole.ATTENDEE,
//                1L,
//                "Looking forward to seeing you!"
//        );
//    }
//
//    @Test
//    void sendEventInvitation_Success() throws JsonProcessingException {
//
//        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
//        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
//        when(eventInvitationRepository.existsByEventIdAndInviteeEmail(1L, "killer@example.com"))
//                .thenReturn(false);
//
//        EventInvitation savedInvitation = EventInvitation.builder()
//                .id(1L)
//                .invitationToken("test-token-123")
//                .inviteeName("Killer Ntua")
//                .inviteeEmail("killer@example.com")
//                .invitationTitle("You're Invited!")
//                .build();
//
//        when(eventInvitationRepository.save(any(EventInvitation.class)))
//                .thenReturn(savedInvitation);
//        when(objectMapper.writeValueAsString(any(EventInvitationEvent.class)))
//                .thenReturn("{\"email\":\"killer@example.com\"}");
//
//
//        invitationService.sendEventInvitation(request);
//
//
//        verify(securityUtils).getCurrentUser();
//        verify(eventRepository).findById(1L);
//        verify(eventInvitationRepository).existsByEventIdAndInviteeEmail(1L, "killer@example.com");
//        verify(eventInvitationRepository).save(any(EventInvitation.class));
//        verify(sqsClient).sendMessage(any(SendMessageRequest.class));
//    }
//
//
//
//
//
//
//    @Test
//    void sendEventInvitation_SqsException() throws JsonProcessingException {
//        // Arrange
//        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
//        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
//        when(eventInvitationRepository.existsByEventIdAndInviteeEmail(1L, "killer@example.com"))
//                .thenReturn(false);
//
//        EventInvitation savedInvitation = EventInvitation.builder()
//                .id(1L)
//                .invitationToken("test-token-123")
//                .inviteeName("Killer Ntua")
//                .inviteeEmail("killer@example.com")
//                .build();
//
//        when(eventInvitationRepository.save(any(EventInvitation.class)))
//                .thenReturn(savedInvitation);
//        when(objectMapper.writeValueAsString(any(EventInvitationEvent.class)))
//                .thenReturn("{\"email\":\"killer@example.com\"}");
//        when(sqsClient.sendMessage(any(SendMessageRequest.class)))
//                .thenThrow(new RuntimeException("SQS error"));
//
//        // Act & Assert
//        InvitationPublishException exception = assertThrows(
//                InvitationPublishException.class,
//                () -> invitationService.sendEventInvitation(request)
//        );
//
//        assertEquals("Failed to publish invitation email event", exception.getMessage());
//    }
//
//    @Test
//    void sendEventInvitation_VerifyInvitationData() throws JsonProcessingException {
//        // Arrange
//        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
//        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
//        when(eventInvitationRepository.existsByEventIdAndInviteeEmail(1L, "killer@example.com"))
//                .thenReturn(false);
//
//        EventInvitation savedInvitation = EventInvitation.builder()
//                .id(1L)
//                .invitationToken("test-token-123")
//                .inviteeName("Killer Ntua")
//                .inviteeEmail("killer@example.com")
//                .invitationTitle("You're Invited!")
//                .build();
//
//        when(eventInvitationRepository.save(any(EventInvitation.class)))
//                .thenReturn(savedInvitation);
//        when(objectMapper.writeValueAsString(any(EventInvitationEvent.class)))
//                .thenReturn("{\"email\":\"killer@example.com\"}");
//
//
//        invitationService.sendEventInvitation(request);
//
//
//        ArgumentCaptor<EventInvitation> invitationCaptor = ArgumentCaptor.forClass(EventInvitation.class);
//        verify(eventInvitationRepository).save(invitationCaptor.capture());
//
//        EventInvitation capturedInvitation = invitationCaptor.getValue();
//        assertEquals("Killer Ntua", capturedInvitation.getInviteeName());
//        assertEquals("killer@example.com", capturedInvitation.getInviteeEmail());
//        assertEquals("NSMQ 2025", capturedInvitation.getInvitationTitle());
//        assertEquals(InviteeRole.ATTENDEE, capturedInvitation.getRole());
//        assertEquals("Looking forward to seeing you!", capturedInvitation.getMessage());
//        assertEquals(1L, capturedInvitation.getInviterId());
//        assertEquals(event, capturedInvitation.getEvent());
//        assertNotNull(capturedInvitation.getInvitationToken());
//        assertNotNull(capturedInvitation.getExpiresAt());
//    }
//
//    @Test
//    void sendEventInvitation_VerifySqsMessage() throws JsonProcessingException {
//
//        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
//        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
//        when(eventInvitationRepository.existsByEventIdAndInviteeEmail(1L, "killer@example.com"))
//                .thenReturn(false);
//
//        EventInvitation savedInvitation = EventInvitation.builder()
//                .id(1L)
//                .invitationToken("test-token-123")
//                .inviteeName("Killer Ntua")
//                .inviteeEmail("killer@example.com")
//                .invitationTitle("You're Invited!")
//                .build();
//
//        when(eventInvitationRepository.save(any(EventInvitation.class)))
//                .thenReturn(savedInvitation);
//        when(objectMapper.writeValueAsString(any(EventInvitationEvent.class)))
//                .thenReturn("{\"email\":\"killer@example.com\"}");
//
//
//        invitationService.sendEventInvitation(request);
//
//
//        ArgumentCaptor<SendMessageRequest> sqsCaptor = ArgumentCaptor.forClass(SendMessageRequest.class);
//        verify(sqsClient).sendMessage(sqsCaptor.capture());
//
//        SendMessageRequest capturedRequest = sqsCaptor.getValue();
//        assertEquals(QUEUE_URL, capturedRequest.queueUrl());
//        assertEquals("{\"email\":\"killer@example.com\"}", capturedRequest.messageBody());
//    }
//
//    @Test
//    void sendEventInvitation_VerifyInvitationLink() throws JsonProcessingException {
//
//        when(securityUtils.getCurrentUser()).thenReturn(currentUser);
//        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
//        when(eventInvitationRepository.existsByEventIdAndInviteeEmail(1L, "killer@example.com"))
//                .thenReturn(false);
//
//        EventInvitation savedInvitation = EventInvitation.builder()
//                .id(1L)
//                .invitationToken("test-token-123")
//                .inviteeName("Killer Ntua")
//                .inviteeEmail("killer@example.com")
//                .invitationTitle("You're Invited!")
//                .build();
//
//        when(eventInvitationRepository.save(any(EventInvitation.class)))
//                .thenReturn(savedInvitation);
//        when(objectMapper.writeValueAsString(any(EventInvitationEvent.class)))
//                .thenReturn("{\"link\":\"test\"}");
//
//
//        invitationService.sendEventInvitation(request);
//
//
//        ArgumentCaptor<EventInvitationEvent> eventCaptor = ArgumentCaptor.forClass(EventInvitationEvent.class);
//        verify(objectMapper).writeValueAsString(eventCaptor.capture());
//
//        EventInvitationEvent capturedEvent = eventCaptor.getValue();
//        String expectedLink = FRONTEND_BASE_URL + "/invitations/accept?token=test-token-123";
//        assertEquals(expectedLink, capturedEvent.inviteLink());
//        assertEquals("You're Invited!", capturedEvent.eventTitle());
//        assertEquals("Killer Ntua", capturedEvent.inviteeName());
//        assertEquals("killer@example.com", capturedEvent.inviteeEmail());
//    }
//}