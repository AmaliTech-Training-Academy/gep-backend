package com.event_service.event_service.integrations;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EventTypeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private static final String API_ENDPOINT = "/api/v1/event_types";
    private static final String VALID_CREATE_BODY = """
            { "name": "DAY_EVENT" } 
        """;
    private static final String VALID_UPDATE_BODY = """
            { "name": "MULTI_DAY_EVENT" } 
        """;


    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanCreateEventType() throws Exception {
        mockMvc.perform(post(API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CREATE_BODY))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanUpdateEventType() throws Exception {
        mockMvc.perform(put(API_ENDPOINT + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_BODY))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCanGetAllEventTypes() throws Exception {
        mockMvc.perform(get(API_ENDPOINT))
                .andExpect(status().isOk());
    }


    @Test
    @WithMockUser(roles = "USER")
    void userCannotCreateEventType() throws Exception {
        mockMvc.perform(post(API_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_CREATE_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotUpdateEventType() throws Exception {
        mockMvc.perform(put(API_ENDPOINT + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_UPDATE_BODY))
                .andExpect(status().isForbidden());
    }
}