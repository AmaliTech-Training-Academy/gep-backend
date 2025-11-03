package com.event_service.event_service.integrations;



import com.event_service.event_service.repositories.EventMeetingTypeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class EventMeetingTypeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventMeetingTypeRepository eventMeetingTypeRepository;

    @BeforeEach
    void setup() {
        eventMeetingTypeRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanGetMeetingTypes() throws Exception {
        mockMvc.perform(get("/api/v1/event_meeting_types"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateMeetingType() throws Exception {
        String body = """
            { "name": "VIRTUAL" }
        """;

        mockMvc.perform(post("/api/v1/event_meeting_types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateMeetingType() throws Exception {
        String body = """
            { "name": "IN_PERSON" }
        """;

        mockMvc.perform(post("/api/v1/event_meeting_types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUpdateMeetingType() throws Exception {
        // Create one first
        String createBody = """
            { "name": "IN_PERSON" }
        """;

        mockMvc.perform(post("/api/v1/event_meeting_types")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Long id = eventMeetingTypeRepository.findAll().getFirst().getId();

        String updateBody = """
            { "name": "VIRTUAL" }
        """;

        mockMvc.perform(put("/api/v1/event_meeting_types/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotUpdateMeetingType() throws Exception {
        String createBody = """
            { "name": "IN_PERSON" }
        """;

        // Create using admin privilege temporarily
        mockMvc.perform(post("/api/v1/event_meeting_types")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Long id = eventMeetingTypeRepository.findAll().getFirst().getId();

        String updateBody = """
            { "name": "VIRTUAL" }
        """;

        mockMvc.perform(put("/api/v1/event_meeting_types/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isForbidden());
    }
}
