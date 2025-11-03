package com.event_service.event_service.integrations;

import com.event_service.event_service.models.EventMeetingType;
import com.event_service.event_service.models.EventType;
import com.event_service.event_service.models.enums.EventMeetingTypeEnum;
import com.event_service.event_service.models.enums.EventTypeEnum;
import com.event_service.event_service.repositories.EventMeetingTypeRepository;
import com.event_service.event_service.repositories.EventRepository;
import com.event_service.event_service.repositories.EventTypeRepository;
import com.event_service.event_service.services.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventCreationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventTypeRepository eventTypeRepository;

    @Autowired
    private EventMeetingTypeRepository eventMeetingTypeRepository;

    @Autowired
    private EventRepository eventRepository;

    @MockitoBean
    private S3Service s3Service;


    private Long dayEventId;
    private Long multiDayEventId;
    private Long inPersonId;
    private Long virtualId;

    private static final String API_ENDPOINT = "/api/v1/events";

    @BeforeEach
    void setup() {
        eventRepository.deleteAll();
        eventMeetingTypeRepository.deleteAll();
        eventTypeRepository.deleteAll();

        EventType dayEvent = EventType.builder().name(EventTypeEnum.DAY_EVENT).build();
        dayEventId = eventTypeRepository.save(dayEvent).getId();
        EventType multiDayEvent = EventType.builder().name(EventTypeEnum.MULTI_DAY_EVENT).build();
        multiDayEventId = eventTypeRepository.save(multiDayEvent).getId();

        EventMeetingType inPerson = EventMeetingType.builder().name(EventMeetingTypeEnum.IN_PERSON).build();
        inPersonId = eventMeetingTypeRepository.save(inPerson).getId();
        EventMeetingType virtual = EventMeetingType.builder().name(EventMeetingTypeEnum.VIRTUAL).build();
        virtualId = eventMeetingTypeRepository.save(virtual).getId();

        MultipartFile dummyFile = new MockMultipartFile(
                "dummy.jpg", "dummy.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy".getBytes()
        );

        Mockito.when(s3Service.uploadImage(Mockito.any(MultipartFile.class)))
                .thenReturn("https://mock-s3.amazonaws.com/test.jpg");

        Mockito.when(s3Service.uploadImages(Mockito.anyList()))
                .thenReturn(List.of("https://mock-s3.amazonaws.com/test1.jpg",
                        "https://mock-s3.amazonaws.com/test2.jpg"));
    }

    private String formatJsonString(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value + "\"";
    }

    private MockMultipartFile createSingleDayRequestPart(Long eventTypeId, Long meetingTypeId, String location, String zoomUrl) {
        String jsonBody = String.format("""
            {
                "title": "Single Day Event",
                "description": "Short description",
                "event_type_id": %d,
                "event_meeting_type_id": %d,
                "event_date": "01/12/2026",
                "event_time": "10:00",
                "event_time_zone_id": "America/New_York",
                "location": %s,
                "zoomUrl": %s,
                "eventOptionsRequest": {
                    "ticketPrice": 99.99,
                    "requiresApproval": false,
                    "capacity": 500
                }
            }
        """, eventTypeId, meetingTypeId, formatJsonString(location), formatJsonString(zoomUrl));

        return new MockMultipartFile(
                "event",
                "request",
                MediaType.APPLICATION_JSON_VALUE,
                jsonBody.getBytes(StandardCharsets.UTF_8)
        );
    }

    private MockMultipartFile createMultiDayRequestPart(Long eventTypeId, Long meetingTypeId, String location, String zoomUrl) {
        String jsonBody = String.format("""
            {
                "title": "Multi-Day Event",
                "description": "Longer description",
                "event_type_id": %d,
                "event_meeting_type_id": %d,
                "event_start_time_date": "01/12/2026",
                "event_end_time_date": "03/12/2026",
                "event_start_time": "09:00",
                "event_end_time": "17:00",
                "event_start_time_zone_id": "America/New_York",
                "event_end_time_zone_id": "America/Los_Angeles",
                "location": %s,
                "zoomUrl": %s,
                "eventOptionsRequest": {
                    "ticketPrice": 199.99,
                    "requiresApproval": true,
                    "capacity": 200
                }
            }
        """, eventTypeId, meetingTypeId, formatJsonString(location), formatJsonString(zoomUrl));

        return new MockMultipartFile(
                "event",
                "request",
                MediaType.APPLICATION_JSON_VALUE,
                jsonBody.getBytes(StandardCharsets.UTF_8)
        );
    }


    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerCanCreateValidInPersonSingleDayEvent() throws Exception {
        MockMultipartFile requestPart = createSingleDayRequestPart(dayEventId, inPersonId, "NYC Venue", null);
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());
        MockMultipartFile eventImage1 = new MockMultipartFile("eventImages", "img1.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-img1".getBytes());

        mockMvc.perform(multipart(API_ENDPOINT)
                        .file(requestPart)
                        .file(imagePart)
                        .file(eventImage1)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Single Day Event"));

        assertEquals(1, eventRepository.count(), "An event should be saved.");
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerFailsValidationIfInPersonEventLacksLocation() throws Exception {
        MockMultipartFile requestPart = createSingleDayRequestPart(dayEventId, inPersonId, null, null);
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());

        mockMvc.perform(multipart(API_ENDPOINT)
                        .file(requestPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorMessage").value("Validation failed: Event location is required"));
    }


    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerCanCreateValidInPersonMultiDayEvent() throws Exception {
        MockMultipartFile requestPart = createMultiDayRequestPart(multiDayEventId, inPersonId, "Convention Center", null);
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());

        mockMvc.perform(multipart(API_ENDPOINT)
                        .file(requestPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Multi-Day Event"));

        assertEquals(1, eventRepository.count(), "An event should be saved.");
    }


    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerCanCreateValidVirtualSingleDayEvent() throws Exception {
        MockMultipartFile requestPart = createSingleDayRequestPart(dayEventId, virtualId, null, "http://zoom.us/webinar/123");
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());

        mockMvc.perform(multipart(API_ENDPOINT)
                        .file(requestPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingLocation").value("http://zoom.us/webinar/123"));

        assertEquals(1, eventRepository.count(), "An event should be saved.");
    }

    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerFailsValidationIfVirtualEventLacksZoomUrl() throws Exception {
        MockMultipartFile requestPart = createSingleDayRequestPart(dayEventId, virtualId, null, null);
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());

        mockMvc.perform(multipart(API_ENDPOINT)
                        .file(requestPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorMessage").value("Validation failed: Zoom link is required"));
    }

    // --- SCENARIO 4: VIRTUAL MULTI_DAY_EVENT ---

    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerCanCreateValidVirtualMultiDayEvent() throws Exception {
        MockMultipartFile requestPart = createMultiDayRequestPart(multiDayEventId, virtualId, null, "http://teams.com/meeting/456");
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());

        mockMvc.perform(multipart(API_ENDPOINT)
                        .file(requestPart)
                        .file(imagePart)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meetingLocation").value("http://teams.com/meeting/456"));

        assertEquals(1, eventRepository.count(), "An event should be saved.");
    }


    @Test
    @WithMockUser(roles = "ORGANIZER")
    void organizerFailsIfTooManyEventImagesAreUploaded() throws Exception {
        MockMultipartFile requestPart = createSingleDayRequestPart(dayEventId, inPersonId, "Location", null);
        MockMultipartFile imagePart = new MockMultipartFile("image", "flyer.jpg", MediaType.IMAGE_JPEG_VALUE, "dummy-flyer".getBytes());

        MockMultipartFile[] excessEventImages = IntStream.range(0, 6)
                .mapToObj(i -> new MockMultipartFile("eventImages", "img" + i + ".jpg", MediaType.IMAGE_JPEG_VALUE, ("data" + i).getBytes()))
                .toArray(MockMultipartFile[]::new);

        MockMultipartHttpServletRequestBuilder requestBuilder = multipart(API_ENDPOINT);
        requestBuilder.file(requestPart).file(imagePart);

        for (MockMultipartFile file : excessEventImages) {
            requestBuilder.file(file);
        }

        requestBuilder.with(csrf());

        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.errorMessage").value("Validation failed: You can upload a maximum of 5 images per event."));

        assertEquals(0, eventRepository.count(), "No event should be saved due to image limit violation.");
    }
}
